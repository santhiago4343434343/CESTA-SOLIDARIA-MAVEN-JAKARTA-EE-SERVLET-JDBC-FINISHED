package com.example.newproject004;

import java.io.IOException;

import classes.Pessoa;
import dao.PessoaDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

/**
 * Autenticacao por sessao.
 *
 * <p>
 * Este servlet finalmente da USO ao BCrypt que ja existia no projeto:
 * antes {@code Pessoa.verificarSenha} so era chamado nos testes. Agora o
 * login confere a senha digitada contra o hash guardado no banco.
 * </p>
 *
 * <pre>
 *   GET  /login             -> mostra o formulario
 *   GET  /login?acao=sair   -> encerra a sessao (logout)
 *   POST /login             -> confere e-mail + senha e cria a sessao
 * </pre>
 */
@WebServlet("/login")
public class LoginServlet extends HttpServlet {

    private final PessoaDAO dao = new PessoaDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        if ("sair".equals(req.getParameter("acao"))) {
            HttpSession sessao = req.getSession(false);
            if (sessao != null) {
                sessao.invalidate();
            }
            HttpSession nova = req.getSession();
            nova.setAttribute("msg", "Voce saiu da sua conta.");
            nova.setAttribute("msgTipo", "info");
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }
        req.getRequestDispatcher("/WEB-INF/views/login.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String email = req.getParameter("email");
        String senha = req.getParameter("senha");

        Pessoa pessoa = (email == null) ? null : dao.buscarPorEmail(email);

        // Mesma mensagem para "email nao existe" e "senha errada":
        // nao revela a um atacante se o e-mail esta ou nao cadastrado.
        // DEPOIS
        if (pessoa == null || !Pessoa.verificarSenha(senha, pessoa.getSenha())) {
            req.setAttribute("erro", "E-mail ou senha invalidos.");
            req.setAttribute("email", email);
            req.getRequestDispatcher("/WEB-INF/views/login.jsp").forward(req, resp);
            return;
        }

        // Conta ainda nao confirmada por e-mail: barra o login ate ativar.
        if (!dao.emailConfirmado(email)) {
            req.setAttribute("erro", "Confirme seu e-mail antes de entrar. Verifique sua caixa de entrada.");
            req.setAttribute("email", email);
            req.getRequestDispatcher("/WEB-INF/views/login.jsp").forward(req, resp);
            return;
        }

        // Regenera a sessao (boa pratica contra "session fixation").
        HttpSession antiga = req.getSession(false);
        if (antiga != null) {
            antiga.invalidate();
        }
        HttpSession sessao = req.getSession(true);
        sessao.setAttribute("usuarioLogado", pessoa.getNomePessoa());
        sessao.setAttribute("usuarioId", pessoa.getIdPessoa());
        sessao.setAttribute("papel", pessoa.getPapel()); // USER ou ADMIN
        sessao.setAttribute("msg", "Bem-vindo(a), " + pessoa.getNomePessoa() + "!");
        sessao.setAttribute("msgTipo", "sucesso");

        // ADMIN vai para a gestao; USER vai para a propria area ("Meu cadastro").
        String destino = pessoa.isAdmin() ? "/pessoas" : "/perfil";
        resp.sendRedirect(req.getContextPath() + destino);
    }
}
