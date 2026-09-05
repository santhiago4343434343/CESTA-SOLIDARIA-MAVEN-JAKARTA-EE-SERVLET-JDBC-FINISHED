<%@ page contentType="text/html;charset=UTF-8" %>
    <%@ taglib prefix="c" uri="jakarta.tags.core" %>
        <c:set var="tituloPagina" value="Novo Cadastro" />
        <%@ include file="/WEB-INF/views/layout/header.jspf" %>
            <script src="https://www.google.com/recaptcha/api.js" async defer></script>

            <div class="card">
                <h2>Novo cadastro</h2>

                <%-- Erro de validacao (ex.: senhas diferentes). Vem no request. --%>
                    <c:if test="${not empty erro}">
                        <div class="alert alert-erro">
                            <c:out value="${erro}" />
                        </div>
                    </c:if>

                    <form method="post" action="${pageContext.request.contextPath}/pessoas">
                        <input type="hidden" name="acao" value="salvar">

                        <div class="form-grupo">
                            <label for="nome">Nome</label>
                            <%-- value com c:out para preservar o que foi digitado sem abrir XSS --%>
                                <input type="text" id="nome" name="nome" value="<c:out value='${nome}'/>" required
                                    autofocus>
                        </div>

                        <div class="form-grupo">
                            <label for="email">E-mail</label>
                            <input type="email" id="email" name="email" value="<c:out value='${email}'/>" required>
                        </div>


                        <div class="form-grupo">
                            <label for="senha">Senha</label>
                            <div style="position:relative">
                                <input type="password" id="senha" name="senha" required minlength="4"
                                    style="padding-right:40px">
                                <button type="button" onclick="verSenha('senha', this)"
                                    style="position:absolute;right:8px;top:50%;transform:translateY(-50%);background:none;border:none;cursor:pointer;color:#555"><svg
                                        width="18" height="18" viewBox="0 0 24 24" fill="none"
                                        xmlns="http://www.w3.org/2000/svg">
                                        <path d="M1 12s4-7 11-7 11 7 11 7-4 7-11 7-11-7-11-7z" stroke="currentColor"
                                            stroke-width="1.6" />
                                        <circle cx="12" cy="12" r="3" stroke="currentColor" stroke-width="1.6" />
                                    </svg></button>
                            </div>
                        </div>

                        <div class="form-grupo">
                            <label for="confirmaSenha">Confirme a senha</label>
                            <div style="position:relative">
                                <input type="password" id="confirmaSenha" name="confirmaSenha" required minlength="4"
                                    style="padding-right:40px">
                                <button type="button" onclick="verSenha('confirmaSenha', this)"
                                    style="position:absolute;right:8px;top:50%;transform:translateY(-50%);background:none;border:none;cursor:pointer;color:#555"><svg
                                        width="18" height="18" viewBox="0 0 24 24" fill="none"
                                        xmlns="http://www.w3.org/2000/svg">
                                        <path d="M1 12s4-7 11-7 11 7 11 7-4 7-11 7-11-7-11-7z" stroke="currentColor"
                                            stroke-width="1.6" />
                                        <circle cx="12" cy="12" r="3" stroke="currentColor" stroke-width="1.6" />
                                    </svg></button>
                            </div>
                        </div>
                        <div class="g-recaptcha" data-sitekey="6LfG9aMtAAAAAP0kmqf_K6Ee4Y3f7n32Hjr2G6Rs"></div>
                        <button class="btn" type="submit">Salvar</button>
                        <a class="btn btn-secundario" href="${pageContext.request.contextPath}/pessoas">Cancelar</a>
                        <a class="btn btn-secundario" href="${pageContext.request.contextPath}/">Início</a>
                    </form>
            </div>

            <%@ include file="/WEB-INF/views/layout/footer.jspf" %>