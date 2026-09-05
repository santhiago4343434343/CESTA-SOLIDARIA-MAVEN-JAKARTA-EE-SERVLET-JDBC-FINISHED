package com.example.newproject004;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.Set;

/**
 * Filtro de AUTORIZACAO (quem pode o que). Roda antes dos servlets protegidos.
 *
 * <p>Regras (garantidas no SERVIDOR, nunca so' escondendo botao na tela):</p>
 * <ul>
 *   <li><b>/perfil</b> ("Meu cadastro") -> qualquer usuario logado (USER ou ADMIN).</li>
 *   <li><b>/pessoas</b> (gestao: listar, editar, excluir, status, detalhes)
 *       -> somente <b>ADMIN</b>.</li>
 *   <li>Excecao: o cadastro publico (acao=novo e acao=salvar) fica liberado,
 *       para o visitante criar a propria conta.</li>
 * </ul>
 *
 * <p>Quem nao esta logado vai para o login; um USER que tenta a area de gestao
 * e' mandado para o seu /perfil com um aviso.</p>
 */
@WebFilter({"/pessoas", "/perfil"})
public class AutenticacaoFilter implements Filter {

    private static final Set<String> ACOES_PUBLICAS = Set.of("novo", "salvar");

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;
        String ctx = req.getContextPath();
        String caminho = req.getServletPath();   // "/pessoas" ou "/perfil"

        boolean logado = estaLogado(req);
        boolean admin = temPapel(req, "ADMIN");

        // ---- Area do usuario: basta estar logado ----
        if ("/perfil".equals(caminho)) {
            if (logado) {
                chain.doFilter(request, response);
            } else {
                flash(req, "info", "Faca login para acessar seu cadastro.");
                resp.sendRedirect(ctx + "/login");
            }
            return;
        }

        // ---- Area de gestao (/pessoas) ----
        String acao = req.getParameter("acao");
        boolean cadastroPublico = acao != null && ACOES_PUBLICAS.contains(acao);
        if (cadastroPublico) {
            chain.doFilter(request, response);      // visitante criando conta
            return;
        }
        if (!logado) {
            flash(req, "info", "Faca login para continuar.");
            resp.sendRedirect(ctx + "/login");
            return;
        }
        if (!admin) {
            // USER logado tentando a gestao -> manda para a area dele
            flash(req, "erro", "Area restrita ao administrador.");
            resp.sendRedirect(ctx + "/perfil");
            return;
        }
        chain.doFilter(request, response);          // ADMIN autorizado
    }

    private boolean estaLogado(HttpServletRequest req) {
        HttpSession s = req.getSession(false);
        return s != null && s.getAttribute("usuarioLogado") != null;
    }

    private boolean temPapel(HttpServletRequest req, String papel) {
        HttpSession s = req.getSession(false);
        return s != null && papel.equals(s.getAttribute("papel"));
    }

    private void flash(HttpServletRequest req, String tipo, String msg) {
        HttpSession s = req.getSession();
        s.setAttribute("msg", msg);
        s.setAttribute("msgTipo", tipo);
    }
}

