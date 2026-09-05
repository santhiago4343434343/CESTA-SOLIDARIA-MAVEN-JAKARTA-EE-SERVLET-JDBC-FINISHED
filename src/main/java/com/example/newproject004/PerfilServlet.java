package com.example.newproject004;

import classes.Pessoa;
import dao.PessoaDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

/**
 * Area "Meu cadastro" do beneficiario (USER) — tambem acessivel ao ADMIN.
 *
 * <p>O usuario ve o proprio cadastro (nome, e-mail e o status em modo leitura)
 * e pode editar os proprios dados e trocar a senha.</p>
 *
 * <p><b>Ponto de seguranca central:</b> todas as operacoes usam o id guardado
 * na SESSAO ({@code usuarioId}), nunca um id vindo do formulario. Assim um
 * usuario nao consegue editar o cadastro de outro trocando um parametro na
 * requisicao (isso evita o ataque conhecido como IDOR). O status NAO e'
 * alterado aqui — isso e' exclusivo do ADMIN.</p>
 */
@WebServlet("/perfil")
public class PerfilServlet extends HttpServlet {

    private final PessoaDAO dao = new PessoaDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        Pessoa eu = dao.buscarPorId(idDaSessao(req));
        req.setAttribute("pessoa", eu);
        req.getRequestDispatcher("/WEB-INF/views/perfil.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        int id = idDaSessao(req);                 // SEMPRE o id da sessao
        String acao = req.getParameter("acao");

        if ("excluir".equals(acao)) {
            excluirConta(req, resp, id);           // faz o proprio redirect
            return;
        }
        if ("senha".equals(acao)) {
            trocarSenha(req, id);
        } else {                                   // "dados"
            atualizarDados(req, id);
        }
        resp.sendRedirect(req.getContextPath() + "/perfil");
    }

    /** Atualiza nome e e-mail do proprio usuario. */
    private void atualizarDados(HttpServletRequest req, int id) {
        String nome = req.getParameter("nome");
        String email = req.getParameter("email");
        if (vazio(nome) || vazio(email)) {
            flash(req, "erro", "Preencha nome e e-mail.");
            return;
        }
        Pessoa p = new Pessoa();
        p.setIdPessoa(id);                         // id da sessao, nao do form
        p.setNomePessoa(nome);
        p.setEmail(email);
        dao.atualizar(p);

        // reflete o novo nome na saudacao da navbar
        req.getSession().setAttribute("usuarioLogado", nome);
        flash(req, "sucesso", "Dados atualizados com sucesso.");
    }

    /** Troca a senha, conferindo a senha atual antes. */
    private void trocarSenha(HttpServletRequest req, int id) {
        String atual = req.getParameter("senhaAtual");
        String nova = req.getParameter("novaSenha");
        String confirma = req.getParameter("confirmaSenha");

        String hashAtual = dao.buscarHashSenha(id);
        if (!Pessoa.verificarSenha(atual, hashAtual)) {
            flash(req, "erro", "Senha atual incorreta.");
            return;
        }
        if (vazio(nova) || nova.length() < 4 || !nova.equals(confirma)) {
            flash(req, "erro", "A nova senha e a confirmacao nao conferem (min. 4 caracteres).");
            return;
        }
        dao.atualizarSenha(id, nova);
        flash(req, "sucesso", "Senha alterada com sucesso.");
    }

    /** Exclui a propria conta, apos conferir a senha atual (re-autenticacao). */
    private void excluirConta(HttpServletRequest req, HttpServletResponse resp, int id)
            throws IOException {
        String senha = req.getParameter("senhaExclusao");
        if (!Pessoa.verificarSenha(senha, dao.buscarHashSenha(id))) {
            flash(req, "erro", "Senha incorreta. A conta NAO foi excluida.");
            resp.sendRedirect(req.getContextPath() + "/perfil");
            return;
        }
        dao.excluir(id);                           // apaga pelo id da SESSAO (evita IDOR)
        HttpSession s = req.getSession(false);
        if (s != null) s.invalidate();             // encerra a sessao
        HttpSession nova = req.getSession();        // nova so' pra exibir o aviso
        nova.setAttribute("msg", "Sua conta foi excluida. Ate breve!");
        nova.setAttribute("msgTipo", "info");
        resp.sendRedirect(req.getContextPath() + "/login");
    }

    // ------------------------------------------------------------- helpers
    private int idDaSessao(HttpServletRequest req) {
        HttpSession s = req.getSession(false);
        Object id = (s == null) ? null : s.getAttribute("usuarioId");
        return (id instanceof Integer) ? (Integer) id : 0;
    }

    private boolean vazio(String s) {
        return s == null || s.isBlank();
    }

    private void flash(HttpServletRequest req, String tipo, String msg) {
        HttpSession s = req.getSession();
        s.setAttribute("msg", msg);
        s.setAttribute("msgTipo", tipo);
    }
}

