package com.example.newproject004;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

import classes.Pessoa;
import dao.PessoaDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import utils.EmailService;

/**
 * Controlador (padrao MVC) das operacoes de Pessoa.
 * Recebe a requisicao, chama o PessoaDAO e encaminha para uma view (JSP)
 * que so exibe os dados. Nenhuma regra fica dentro do JSP.
 *
 * <p>
 * Rotas (parametro "acao"):
 * </p>
 * 
 * <pre>
 *   GET  /pessoas                          -> lista (com busca e paginacao)
 *   GET  /pessoas?acao=novo                -> formulario de cadastro
 *   GET  /pessoas?acao=editar&id=..        -> formulario de edicao
 *   GET  /pessoas?acao=detalhes&id=..      -> detalhes de uma pessoa
 *   POST /pessoas (acao=salvar)            -> insere
 *   POST /pessoas (acao=atualizar)         -> atualiza
 *   POST /pessoas (acao=excluir)           -> exclui
 * </pre>
 */
@WebServlet("/pessoas")
public class PessoaServlet extends HttpServlet {

    /** Quantidade de registros por pagina na listagem. */
    private static final int TAMANHO_PAGINA = 5;

    private final PessoaDAO dao = new PessoaDAO();

    // ------------------------------------------------------------------ GET
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String acao = req.getParameter("acao");
        if (acao == null) {
            acao = "listar";
        }

        switch (acao) {
            case "novo":
                encaminhar(req, resp, "/WEB-INF/views/cadastrar.jsp");
                break;
            case "editar":
                req.setAttribute("pessoa", dao.buscarPorId(lerId(req)));
                encaminhar(req, resp, "/WEB-INF/views/editar.jsp");
                break;
            case "detalhes":
                req.setAttribute("pessoa", dao.buscarPorId(lerId(req)));
                encaminhar(req, resp, "/WEB-INF/views/detalhes.jsp");
                break;
            default:
                listar(req, resp);
        }
    }

    /** Monta a listagem com busca e paginacao. */
    private void listar(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String busca = req.getParameter("busca");
        int pagina = lerInt(req.getParameter("pagina"), 1);
        if (pagina < 1) {
            pagina = 1;
        }

        int total = dao.contar(busca);
        int totalPaginas = (int) Math.ceil((double) total / TAMANHO_PAGINA);
        if (totalPaginas == 0) {
            totalPaginas = 1;
        }
        if (pagina > totalPaginas) {
            pagina = totalPaginas;
        }

        int offset = (pagina - 1) * TAMANHO_PAGINA;
        List<Pessoa> pessoas = dao.listar(busca, offset, TAMANHO_PAGINA);

        req.setAttribute("pessoas", pessoas);
        req.setAttribute("busca", busca == null ? "" : busca);
        req.setAttribute("pagina", pagina);
        req.setAttribute("totalPaginas", totalPaginas);
        encaminhar(req, resp, "/WEB-INF/views/listar.jsp");
    }

    // ----------------------------------------------------------------- POST
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String acao = req.getParameter("acao");
        if (acao == null) {
            acao = "";
        }

        switch (acao) {
            case "atualizar":
                atualizar(req, resp);
                break;
            case "excluir":
                dao.excluir(lerId(req));
                flash(req, "sucesso", "Pessoa excluida com sucesso.");
                resp.sendRedirect(req.getContextPath() + "/pessoas");
                break;
            case "salvar":
                salvar(req, resp);
                break;
            case "status":
                alterarStatus(req, resp);
                break;
            default:
                resp.sendRedirect(req.getContextPath() + "/pessoas");
        }
    }

    private void salvar(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String nome = req.getParameter("nome");
        String email = req.getParameter("email");
        String senha = req.getParameter("senha");
        String confirma = req.getParameter("confirmaSenha");
        String captcha = req.getParameter("g-recaptcha-response");

        // Valida o captcha antes de qualquer outra coisa.
        if (!validarCaptcha(captcha)) {
            reexibirCadastro(req, resp, nome, email, "Confirme que voce nao e' um robo.");
            return;
        }

        // Validacao no servidor (nao confia so no "required" do HTML).
        if (vazio(nome) || vazio(email) || vazio(senha)) {
            reexibirCadastro(req, resp, nome, email, "Preencha todos os campos.");
            return;
        }
        if (!senha.equals(confirma)) {
            reexibirCadastro(req, resp, nome, email, "As senhas nao conferem.");
            return;
        }

        // >>> trava de email duplicado <<<
        if (dao.buscarPorEmail(email) != null) {
            reexibirCadastro(req, resp, nome, email, "Este e-mail ja esta cadastrado.");
            return;
        }

        Pessoa p = new Pessoa();
        p.setNomePessoa(nome);
        p.setEmail(email);
        p.setSenha(senha);
        boolean inserido = dao.inserir(p);

        // Apos cadastrar, gera o token e dispara (em segundo plano) o e-mail de
        // ATIVACAO.
        if (inserido) {
            String token = UUID.randomUUID().toString().replace("-", "");
            dao.salvarTokenConfirmacao(email, token);
            String base = req.getScheme() + "://" + req.getServerName()
                    + ":" + req.getServerPort() + req.getContextPath();
            String link = base + "/confirmar?token=" + token;
            EmailService.enviarAtivacaoAsync(nome, email, link);
        }

        // Agora a mensagem avisa pra conferir o e-mail e ativar.
        if (estaLogado(req)) {
            flash(req, "sucesso", "Pessoa cadastrada. Um e-mail de ativacao foi enviado.");
            resp.sendRedirect(req.getContextPath() + "/pessoas");
        } else {
            flash(req, "sucesso", "Conta criada! Enviamos um link de ativacao para o seu e-mail.");
            resp.sendRedirect(req.getContextPath() + "/login");
        }
    }

    private void atualizar(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {

        Pessoa p = new Pessoa();
        p.setIdPessoa(lerId(req));
        p.setNomePessoa(req.getParameter("nome"));
        p.setEmail(req.getParameter("email"));
        dao.atualizar(p);
        flash(req, "sucesso", "Dados atualizados com sucesso.");
        resp.sendRedirect(req.getContextPath() + "/pessoas");
    }

    /** Alterna a situacao da pessoa na fila: aguardando <-> retirado. */
    private void alterarStatus(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        String novo = req.getParameter("novo");
        // So aceita os dois valores validos (nunca confie no que vem do cliente).
        if (!"aguardando".equals(novo) && !"retirado".equals(novo)) {
            novo = "aguardando";
        }
        dao.atualizarStatus(lerId(req), novo);
        flash(req, "sucesso", "retirado".equals(novo)
                ? "Cesta marcada como retirada."
                : "Status voltou para aguardando.");
        resp.sendRedirect(req.getContextPath() + "/pessoas");
    }

    private void reexibirCadastro(HttpServletRequest req, HttpServletResponse resp,
            String nome, String email, String erro)
            throws ServletException, IOException {
        req.setAttribute("erro", erro);
        req.setAttribute("nome", nome);
        req.setAttribute("email", email);
        encaminhar(req, resp, "/WEB-INF/views/cadastrar.jsp");
    }

    // -------------------------------------------------------------- helpers
    private boolean estaLogado(HttpServletRequest req) {
        HttpSession sessao = req.getSession(false);
        return sessao != null && sessao.getAttribute("usuarioLogado") != null;
    }

    /** Guarda uma mensagem na sessao para exibir apos o redirect (padrao PRG). */
    private void flash(HttpServletRequest req, String tipo, String msg) {
        HttpSession sessao = req.getSession();
        sessao.setAttribute("msg", msg);
        sessao.setAttribute("msgTipo", tipo);
    }

    private boolean vazio(String s) {
        return s == null || s.isBlank();
    }

    /**
     * Valida a resposta do reCAPTCHA junto ao Google (server-to-server),
     * usando a Secret Key configurada na variavel de ambiente RECAPTCHA_SECRET.
     */
    private boolean validarCaptcha(String resposta) {
        if (resposta == null || resposta.isBlank())
            return false;
        try {
            String secret = System.getenv("RECAPTCHA_SECRET");
            String body = "secret=" + java.net.URLEncoder.encode(secret, java.nio.charset.StandardCharsets.UTF_8)
                    + "&response=" + java.net.URLEncoder.encode(resposta, java.nio.charset.StandardCharsets.UTF_8);
            java.net.http.HttpRequest r = java.net.http.HttpRequest.newBuilder()
                    .uri(java.net.URI.create("https://www.google.com/recaptcha/api/siteverify"))
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .POST(java.net.http.HttpRequest.BodyPublishers.ofString(body))
                    .build();
            String json = java.net.http.HttpClient.newHttpClient()
                    .send(r, java.net.http.HttpResponse.BodyHandlers.ofString()).body();
            return json.contains("\"success\": true") || json.contains("\"success\":true");
        } catch (Exception e) {
            System.out.println("captcha: " + e.getMessage());
            return false;
        }
    }

    private int lerId(HttpServletRequest req) {
        return lerInt(req.getParameter("id"), 0);
    }

    /** Converte texto em int com valor padrao (evita NumberFormatException). */
    private int lerInt(String valor, int padrao) {
        try {
            return Integer.parseInt(valor);
        } catch (NumberFormatException e) {
            return padrao;
        }
    }

    private void encaminhar(HttpServletRequest req, HttpServletResponse resp, String view)
            throws ServletException, IOException {
        req.getRequestDispatcher(view).forward(req, resp);
    }
}
