<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="tituloPagina" value="Esqueci minha senha" />
<%@ include file="/WEB-INF/views/layout/header.jspf" %>

<div class="card" style="max-width:420px;margin:0 auto;">
    <h2>Esqueci minha senha</h2>
    <p class="texto-suave">Informe seu e-mail. Se estiver cadastrado, enviaremos um link para redefinir a senha.</p>

    <c:if test="${not empty msg}">
        <div class="alert alert-info"><c:out value="${msg}" /></div>
    </c:if>
    <c:if test="${not empty erro}">
        <div class="alert alert-erro"><c:out value="${erro}" /></div>
    </c:if>

    <form method="post" action="${pageContext.request.contextPath}/esqueci">
        <div class="form-grupo">
            <label for="email">E-mail</label>
            <input type="email" id="email" name="email" required autofocus>
        </div>
        <button class="btn" type="submit">Enviar link de redefinição</button>
    </form>

    <p class="mt texto-suave">
        <a href="${pageContext.request.contextPath}/login">Voltar para o login</a>
    </p>
</div>

<%@ include file="/WEB-INF/views/layout/footer.jspf" %>
