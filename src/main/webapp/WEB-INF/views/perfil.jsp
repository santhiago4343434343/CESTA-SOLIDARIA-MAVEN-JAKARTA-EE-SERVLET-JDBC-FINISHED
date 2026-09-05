<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="tituloPagina" value="Meu Cadastro" />
<%@ include file="/WEB-INF/views/layout/header.jspf" %>

<h2>Meu cadastro</h2>

<c:choose>
    <c:when test="${pessoa != null}">

        <%-- ---- Situacao na fila (somente leitura; quem muda e' o admin) ---- --%>
        <div class="card">
            <h3 style="margin-top:0;">Situacao na fila da cesta</h3>
            <p>
                <c:choose>
                    <c:when test="${pessoa.status == 'retirado'}">
                        <span class="selo selo-retirado">Retirado</span>
                    </c:when>
                    <c:otherwise>
                        <span class="selo selo-aguardando">Aguardando</span>
                    </c:otherwise>
                </c:choose>
            </p>
            <p class="texto-suave">
                A situacao e' definida pela organizacao (administrador). Se estiver
                "Aguardando", e' so' aguardar a chamada para a retirada.
            </p>
        </div>

        <%-- ---- Dados pessoais (o proprio usuario edita) ---- --%>
        <div class="card mt">
            <h3 style="margin-top:0;">Dados pessoais</h3>
            <form method="post" action="${pageContext.request.contextPath}/perfil">
                <input type="hidden" name="acao" value="dados">
                <div class="form-grupo">
                    <label for="nome">Nome</label>
                    <input type="text" id="nome" name="nome"
                           value="<c:out value='${pessoa.nomePessoa}'/>" required>
                </div>
                <div class="form-grupo">
                    <label for="email">E-mail</label>
                    <input type="email" id="email" name="email"
                           value="<c:out value='${pessoa.email}'/>" required>
                </div>
                <button class="btn" type="submit">Salvar dados</button>
            </form>
        </div>

        <%-- ---- Troca de senha (confere a senha atual) ---- --%>
        <div class="card mt">
            <h3 style="margin-top:0;">Trocar senha</h3>
            <form method="post" action="${pageContext.request.contextPath}/perfil">
                <input type="hidden" name="acao" value="senha">

                <div class="form-grupo">
                    <label for="senhaAtual">Senha atual</label>
                    <div style="position:relative">
                        <input type="password" id="senhaAtual" name="senhaAtual" required style="padding-right:40px">
                        <button type="button" onclick="verSenha('senhaAtual', this)"
                                style="position:absolute;right:8px;top:50%;transform:translateY(-50%);background:none;border:none;cursor:pointer;color:#555">
                            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg"><path d="M1 12s4-7 11-7 11 7 11 7-4 7-11 7-11-7-11-7z" stroke="currentColor" stroke-width="1.6"/><circle cx="12" cy="12" r="3" stroke="currentColor" stroke-width="1.6"/></svg>
                        </button>
                    </div>
                </div>

                <div class="form-grupo">
                    <label for="novaSenha">Nova senha</label>
                    <div style="position:relative">
                        <input type="password" id="novaSenha" name="novaSenha" required minlength="4" style="padding-right:40px">
                        <button type="button" onclick="verSenha('novaSenha', this)"
                                style="position:absolute;right:8px;top:50%;transform:translateY(-50%);background:none;border:none;cursor:pointer;color:#555">
                            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg"><path d="M1 12s4-7 11-7 11 7 11 7-4 7-11 7-11-7-11-7z" stroke="currentColor" stroke-width="1.6"/><circle cx="12" cy="12" r="3" stroke="currentColor" stroke-width="1.6"/></svg>
                        </button>
                    </div>
                </div>

                <div class="form-grupo">
                    <label for="confirmaSenha">Confirme a nova senha</label>
                    <div style="position:relative">
                        <input type="password" id="confirmaSenha" name="confirmaSenha" required minlength="4" style="padding-right:40px">
                        <button type="button" onclick="verSenha('confirmaSenha', this)"
                                style="position:absolute;right:8px;top:50%;transform:translateY(-50%);background:none;border:none;cursor:pointer;color:#555">
                            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg"><path d="M1 12s4-7 11-7 11 7 11 7-4 7-11 7-11-7-11-7z" stroke="currentColor" stroke-width="1.6"/><circle cx="12" cy="12" r="3" stroke="currentColor" stroke-width="1.6"/></svg>
                        </button>
                    </div>
                </div>

                <button class="btn btn-aviso" type="submit">Alterar senha</button>
            </form>
        </div>

        <%-- ---- Excluir conta (abre um modal e confere a senha) ---- --%>
        <div class="card mt" style="border-color:#dc3545;">
            <h3 style="margin-top:0;color:#dc3545;">Excluir conta</h3>
            <p class="texto-suave">Essa acao e' permanente e nao pode ser desfeita.</p>
            <button type="button" class="btn btn-perigo" onclick="abrirModalExclusao()">Excluir minha conta</button>
        </div>

        <div id="modalExclusao"
             style="display:none;position:fixed;inset:0;background:rgba(0,0,0,.5);z-index:1000;align-items:center;justify-content:center;">
            <div class="card" style="max-width:400px;">
                <h3 style="margin-top:0;">Confirmar exclusao</h3>
                <p class="texto-suave">Digite sua senha atual para confirmar. Essa acao e' irreversivel.</p>
                <form method="post" action="${pageContext.request.contextPath}/perfil">
                    <input type="hidden" name="acao" value="excluir">
                    <div class="form-grupo">
                        <label for="senhaExclusao">Senha atual</label>
                        <div style="position:relative">
                            <input type="password" id="senhaExclusao" name="senhaExclusao" required style="padding-right:40px">
                            <button type="button" onclick="verSenha('senhaExclusao', this)"
                                    style="position:absolute;right:8px;top:50%;transform:translateY(-50%);background:none;border:none;cursor:pointer;color:#555">
                                <svg width="18" height="18" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg"><path d="M1 12s4-7 11-7 11 7 11 7-4 7-11 7-11-7-11-7z" stroke="currentColor" stroke-width="1.6"/><circle cx="12" cy="12" r="3" stroke="currentColor" stroke-width="1.6"/></svg>
                            </button>
                        </div>
                    </div>
                    <button class="btn btn-perigo" type="submit">Confirmar exclusao</button>
                    <button class="btn btn-secundario" type="button" onclick="fecharModalExclusao()">Cancelar</button>
                </form>
            </div>
        </div>

        <script>
            function abrirModalExclusao() { document.getElementById('modalExclusao').style.display = 'flex'; }
            function fecharModalExclusao() { document.getElementById('modalExclusao').style.display = 'none'; }
        </script>

    </c:when>
    <c:otherwise>
        <div class="alert alert-erro">Nao foi possivel carregar seu cadastro.</div>
    </c:otherwise>
</c:choose>

<%@ include file="/WEB-INF/views/layout/footer.jspf" %>
