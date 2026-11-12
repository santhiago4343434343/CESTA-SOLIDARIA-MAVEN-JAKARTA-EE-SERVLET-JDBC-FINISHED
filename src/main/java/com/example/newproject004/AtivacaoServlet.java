package com.example.newproject004;

import dao.PessoaDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

/**
 * Ativacao de conta por e-mail.
 *
 * <pre>
 *   GET /confirmar?token=...  -> valida o token e ativa a conta
 * </pre>
 *
 * <p>O link chega pelo e-mail (metodo GET). Se o token bater, a conta
 * vira ativa e o token e' apagado (nao serve 2x). Depois manda pro login.</p>
 */
@WebServlet("/confirmar")
public class AtivacaoServlet extends HttpServlet {

    private final PessoaDAO dao = new PessoaDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String token = req.getParameter("token");
        boolean ativou = (token != null && !token.isBlank()) && dao.confirmarEmailPorToken(token);

        HttpSession sessao = req.getSession();
        if (ativou) {
            sessao.setAttribute("msg", "Conta ativada com sucesso! Agora voce ja pode entrar.");
            sessao.setAttribute("msgTipo", "sucesso");
        } else {
            sessao.setAttribute("msg", "Link de ativacao invalido ou ja utilizado.");
            sessao.setAttribute("msgTipo", "erro");
        }
        resp.sendRedirect(req.getContextPath() + "/login");
    }
}
