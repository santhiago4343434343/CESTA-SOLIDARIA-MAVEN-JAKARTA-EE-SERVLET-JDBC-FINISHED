<%@ page contentType="text/html;charset=UTF-8" %>
    <%@ taglib prefix="c" uri="jakarta.tags.core" %>
        <c:set var="tituloPagina" value="Entrar" />
        <%@ include file="/WEB-INF/views/layout/header.jspf" %>

            <div class="card" style="max-width:420px;margin:0 auto;">
                <h2>Entrar</h2>
                <p class="texto-suave">Use o e-mail e a senha de uma pessoa cadastrada.</p>

                <c:if test="${not empty erro}">
                    <div class="alert alert-erro">
                        <c:out value="${erro}" />
                    </div>
                </c:if>

                <form method="post" action="${pageContext.request.contextPath}/login">
                    <div class="form-grupo">
                        <label for="email">E-mail</label>
                        <input type="email" id="email" name="email" value="<c:out value='${email}'/>" required
                            autofocus>
                    </div>
                    <div class="form-grupo">
                        <label for="senha">Senha</label>
                        <div style="position:relative">
                            <input type="password" id="senha" name="senha" required style="padding-right:40px">

                            <button type="button" onclick="verSenha('senha', this)"
                                style="position:absolute;right:8px;top:50%;transform:translateY(-50%);background:none;border:none;cursor:pointer;color:#555">
                                <svg width="18" height="18" viewBox="0 0 24 24" fill="none"
                                    xmlns="http://www.w3.org/2000/svg">
                                    <path d="M1 12s4-7 11-7 11 7 11 7-4 7-11 7-11-7-11-7z" stroke="currentColor"
                                        stroke-width="1.6" />
                                    <circle cx="12" cy="12" r="3" stroke="currentColor" stroke-width="1.6" />
                                </svg>
                            </button>
                        </div>
                    </div>
                    <button class="btn" type="submit">Entrar</button>
                </form>

                <p class="mt texto-suave">
                    Nao tem conta?
                    <a href="${pageContext.request.contextPath}/pessoas?acao=novo">Criar cadastro</a>
                </p>
            </div>

            <%@ include file="/WEB-INF/views/layout/footer.jspf" %>