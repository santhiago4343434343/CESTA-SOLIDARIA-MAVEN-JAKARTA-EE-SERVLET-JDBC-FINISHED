<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="tituloPagina" value="Redefinir senha" />
<%@ include file="/WEB-INF/views/layout/header.jspf" %>

<div class="card" style="max-width:420px;margin:0 auto;">
    <h2>Redefinir senha</h2>
    <p class="texto-suave">Crie uma nova senha para sua conta.</p>

    <c:if test="${not empty erro}">
        <div class="alert alert-erro"><c:out value="${erro}" /></div>
    </c:if>

    <form method="post" action="${pageContext.request.contextPath}/redefinir">
        <input type="hidden" name="token" value="<c:out value='${token}'/>">

        <div class="form-grupo">
            <label for="senha">Nova senha</label>
            <div style="position:relative">
                <input type="password" id="senha" name="senha" required minlength="6" style="padding-right:40px">
                <button type="button" onclick="verSenha('senha', this)"
                    style="position:absolute;right:8px;top:50%;transform:translateY(-50%);background:none;border:none;cursor:pointer;color:#555">
                    <svg width="18" height="18" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                        <path d="M1 12s4-7 11-7 11 7 11 7-4 7-11 7-11-7-11-7z" stroke="currentColor" stroke-width="1.6" />
                        <circle cx="12" cy="12" r="3" stroke="currentColor" stroke-width="1.6" />
                    </svg>
                </button>
            </div>
        </div>

        <div class="form-grupo">
            <label for="confirma">Confirme a nova senha</label>
            <div style="position:relative">
                <input type="password" id="confirma" name="confirma" required minlength="6" style="padding-right:40px">
                <button type="button" onclick="verSenha('confirma', this)"
                    style="position:absolute;right:8px;top:50%;transform:translateY(-50%);background:none;border:none;cursor:pointer;color:#555">
                    <svg width="18" height="18" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                        <path d="M1 12s4-7 11-7 11 7 11 7-4 7-11 7-11-7-11-7z" stroke="currentColor" stroke-width="1.6" />
                        <circle cx="12" cy="12" r="3" stroke="currentColor" stroke-width="1.6" />
                    </svg>
                </button>
            </div>
        </div>

        <button class="btn" type="submit">Salvar nova senha</button>
    </form>

    <p class="mt texto-suave">
        <a href="${pageContext.request.contextPath}/login">Voltar para o login</a>
    </p>
</div>

<%@ include file="/WEB-INF/views/layout/footer.jspf" %>
