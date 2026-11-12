package com.example.newproject004;

import classes.Pessoa;
import dao.PessoaDAO;
import utils.EmailService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.UUID;

/**
 * Fluxo de "Esqueci minha senha" (recuperação por e-mail + token).
 *
 * <pre>
 *   GET  /esqueci              -> formulário (pede o e-mail)
 *   POST /esqueci              -> gera token, salva e manda o link por e-mail
 *   GET  /redefinir?token=...  -> formulário de nova senha (se o token valer)
 *   POST /redefinir            -> valida o token e troca a senha
 * </pre>
 *
 * <p>Segurança: a resposta do /esqueci é SEMPRE a mesma (exista o e-mail ou não),
 * para não revelar quais e-mails estão cadastrados.</p>
 */
@WebServlet({"/esqueci", "/redefinir"})
public class SenhaServlet extends HttpServlet {

    private final PessoaDAO dao = new PessoaDAO();
    private static final int VALIDADE_MINUTOS = 60;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        if ("/redefinir".equals(req.getServletPath())) {
            // só mostra o form de nova senha se o token for válido
            String token = req.getParameter("token");
            Pessoa p = (token == null) ? null : dao.buscarPorTokenValido(token);
            if (p == null) {
                req.setAttribute("erro", "Link inválido ou expirado. Peça um novo.");
                req.getRequestDispatcher("/WEB-INF/views/esqueci.jsp").forward(req, resp);
                return;
            }
            req.setAttribute("token", token);
            req.getRequestDispatcher("/WEB-INF/views/redefinir.jsp").forward(req, resp);
            return;
        }
        // /esqueci
        req.getRequestDispatcher("/WEB-INF/views/esqueci.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        if ("/redefinir".equals(req.getServletPath())) {
            processarRedefinir(req, resp);
        } else {
            processarEsqueci(req, resp);
        }
    }

    /** POST /esqueci -> gera o token e manda o e-mail (resposta sempre igual). */
    private void processarEsqueci(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String email = req.getParameter("email");
        Pessoa p = (email == null || email.isBlank()) ? null : dao.buscarPorEmail(email);

        if (p != null) {
            String token = UUID.randomUUID().toString().replace("-", "");
            if (dao.salvarTokenReset(email, token, VALIDADE_MINUTOS)) {
                String base = req.getScheme() + "://" + req.getServerName()
                        + ":" + req.getServerPort() + req.getContextPath();
                String link = base + "/redefinir?token=" + token;
                EmailService.enviarResetAsync(p.getNomePessoa(), email, link);
            }
        }
        // mensagem IGUAL exista ou não o e-mail (não revela cadastro)
        req.setAttribute("msg", "Se o e-mail estiver cadastrado, enviamos um link de redefinição.");
        req.getRequestDispatcher("/WEB-INF/views/esqueci.jsp").forward(req, resp);
    }

    /** POST /redefinir -> confere o token e troca a senha. */
    private void processarRedefinir(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String token = req.getParameter("token");
        String senha = req.getParameter("senha");
        String confirma = req.getParameter("confirma");

        if (senha == null || senha.length() < 6 || !senha.equals(confirma)) {
            req.setAttribute("erro", "As senhas devem ser iguais e ter ao menos 6 caracteres.");
            req.setAttribute("token", token);
            req.getRequestDispatcher("/WEB-INF/views/redefinir.jsp").forward(req, resp);
            return;
        }

        if (dao.redefinirSenhaPorToken(token, senha)) {
            req.getSession().setAttribute("msg", "Senha redefinida com sucesso! Faça login.");
            req.getSession().setAttribute("msgTipo", "sucesso");
            resp.sendRedirect(req.getContextPath() + "/login");
        } else {
            req.setAttribute("erro", "Link inválido ou expirado. Peça um novo.");
            req.getRequestDispatcher("/WEB-INF/views/esqueci.jsp").forward(req, resp);
        }
    }
}
