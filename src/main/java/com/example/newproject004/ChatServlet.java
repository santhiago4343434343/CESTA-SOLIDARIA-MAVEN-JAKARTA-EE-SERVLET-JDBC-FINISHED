package com.example.newproject004;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import org.json.JSONArray;
import org.json.JSONObject;

import classes.Pessoa;
import dao.PessoaDAO;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

/**
 * Chat do site Cesta Solidaria. Dois caminhos:
 *  - Atalho deterministico: pergunta de status + usuario logado -> responde do banco
 *    (garantido, instantaneo, nao depende da IA).
 *  - IA (Gemini): perguntas abertas, com a base de conhecimento e o contexto do usuario.
 * Seguranca: status so' e' consultado pelo id da SESSAO, nunca por e-mail/CPF digitado.
 */
@WebServlet("/chat")
public class ChatServlet extends HttpServlet {

    private static final String IA_API_KEY = System.getenv("IA_API_KEY");
    private static final String GEMINI_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.6-flash:generateContent";

    private final PessoaDAO dao = new PessoaDAO();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String msg = req.getParameter("msg");
        resp.setContentType("text/plain; charset=UTF-8");

        // 1) Descobre o usuario logado (pela SESSAO, nunca por dado digitado)
        HttpSession sessao = req.getSession(false);
        Object idObj = (sessao == null) ? null : sessao.getAttribute("usuarioId");
        Pessoa usuario = null;
        if (idObj instanceof Integer) {
            try {
                usuario = dao.buscarPorId((Integer) idObj);
            } catch (Exception e) {
                System.out.println("[chat] erro ao buscar usuario: " + e.getMessage());
            }
        }

        // 2) ATALHO DETERMINISTICO: status + logado -> responde direto do banco
        if (usuario != null && ehPerguntaDeStatus(msg)) {
            String frase = "retirado".equals(usuario.getStatus())
                ? "Oi, " + usuario.getNomePessoa() + "! Sua cesta ja foi liberada (status: retirado). "
                  + "Combine a retirada com a organizacao."
                : "Oi, " + usuario.getNomePessoa() + "! Seu status e 'aguardando': voce esta na fila e "
                  + "avisaremos quando a cesta for liberada.";
            resp.getWriter().write(frase);
            return; // resposta garantida, sem depender da IA
        }

        // 3) Daqui pra frente precisa da IA
        if (IA_API_KEY == null || IA_API_KEY.isBlank()) {
            System.out.println("[chat] IA_API_KEY vazia.");
            resp.getWriter().write("O chat ainda nao esta configurado (falta a chave da IA).");
            return;
        }

        try {
            // contexto do usuario (para a IA usar em perguntas abertas)
            String infoUsuario = (usuario != null)
                ? "CONTEXTO DO USUARIO: logado. Nome: " + usuario.getNomePessoa()
                  + ". Status atual: '" + usuario.getStatus() + "'."
                : "CONTEXTO DO USUARIO: nao logado. Se perguntar sobre o proprio cadastro/status, "
                  + "oriente a fazer login primeiro.";

            String contexto =
                "Voce e o assistente virtual do site 'Cesta Solidaria' (lista de espera para retirada "
                + "de cestas basicas). Responda de forma OBJETIVA e CURTA (2 a 3 frases), direto ao "
                + "ponto, em portugues do Brasil, sem listas longas.\n"
                + "FUNCIONALIDADES DO SITE:\n"
                + "- Cadastro: em 'Fazer meu cadastro', com nome, e-mail e senha; a pessoa entra na fila. O site usa o e-mail (NAO usa CPF).\n"
                + "- Login: em 'Entrar', com e-mail e senha.\n"
                + "- Status: comeca em 'aguardando' e muda para 'retirado' quando a cesta e liberada.\n"
                + "- Meu cadastro (apos login): ver o proprio status, editar nome/e-mail, trocar senha e excluir a conta.\n"
                + "- Apos o cadastro e enviado um e-mail de confirmacao.\n"
                + "- O administrador gerencia a fila e altera os status.\n"
                + "REGRAS: nunca invente dados; nao fale de CRAS, prefeitura, supermercado ou outros "
                + "programas; so informe status de quem esta logado, nunca por e-mail/CPF digitado.\n"
                + infoUsuario + "\n\n"
                + "Exemplos de tom (curto e direto):\n"
                + "P: Como me cadastro? R: Clique em 'Fazer meu cadastro', preencha nome, e-mail e senha e envie; pronto, voce entra na fila.\n"
                + "P: Como vejo meu status? R: Faca login e abra 'Meu cadastro'; o status aparece la.\n\n"
                + "Responda a pergunta abaixo de forma curta e direta (sem escrever 'P:' nem 'R:').\n"
                + "Pergunta: " + msg;

            JSONObject part = new JSONObject().put("text", contexto);
            JSONObject content = new JSONObject().put("parts", new JSONArray().put(part));
            JSONObject body = new JSONObject()
                    .put("contents", new JSONArray().put(content))
                    .put("generationConfig", new JSONObject()
                            .put("temperature", 0.3)
                            .put("maxOutputTokens", 500));

            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(GEMINI_URL))
                    .header("Content-Type", "application/json")
                    .header("x-goog-api-key", IA_API_KEY)
                    .POST(HttpRequest.BodyPublishers.ofString(body.toString()))
                    .build();

            HttpResponse<String> response = null;
            for (int tentativa = 1; tentativa <= 3; tentativa++) {
                response = client.send(request, HttpResponse.BodyHandlers.ofString());
                int st = response.statusCode();
                if (st != 503 && st != 429)
                    break;
                System.out.println("[chat] modelo ocupado (" + st + "), tentativa " + tentativa + " de 3...");
                Thread.sleep(1500L * tentativa);
            }

            System.out.println("[chat] status HTTP = " + response.statusCode());
            System.out.println("[chat] resposta = " + response.body());

            JSONObject json = new JSONObject(response.body());
            if (!json.has("candidates")) {
                String motivo = json.has("error")
                        ? json.getJSONObject("error").optString("message", "erro desconhecido")
                        : "resposta inesperada da IA";
                System.out.println("[chat] sem 'candidates'. Motivo: " + motivo);
                resp.getWriter().write("A IA nao respondeu: " + motivo);
                return;
            }

            String texto = json.getJSONArray("candidates")
                    .getJSONObject(0)
                    .getJSONObject("content")
                    .getJSONArray("parts")
                    .getJSONObject(0)
                    .getString("text");
            resp.getWriter().write(texto);

        } catch (Exception e) {
            resp.getWriter().write("Desculpe, houve um erro ao processar sua mensagem.");
            e.printStackTrace();
        }
    }

    /** Detecta se a mensagem e' uma pergunta sobre o proprio status/situacao. */
    private boolean ehPerguntaDeStatus(String msg) {
        if (msg == null) return false;
        String p = msg.toLowerCase();
        return p.contains("status")
            || p.contains("aprovad")       // "fui aprovado?"
            || p.contains("minha cesta")   // "minha cesta saiu?"
            || p.contains("minha situac")  // "minha situacao"
            || p.contains("posso retirar")
            || p.contains("ja liberou");
    }
}