<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="tituloPagina" value="Editar Pessoa"/>
<%@ include file="/WEB-INF/views/layout/header.jspf" %>

<div class="card">
    <h2>Editar cadastro</h2>

    <c:choose>
        <c:when test="${pessoa != null}">
            <form method="post" action="${pageContext.request.contextPath}/pessoas">
                <input type="hidden" name="acao" value="atualizar">
                <input type="hidden" name="id" value="${pessoa.idPessoa}">

                <div class="form-grupo">
                    <label for="nome">Nome</label>
                    <input type="text" id="nome" name="nome"
                           value="<c:out value='${pessoa.nomePessoa}'/>" required autofocus>
                </div>

                <div class="form-grupo">
                    <label for="email">E-mail</label>
                    <input type="email" id="email" name="email"
                           value="<c:out value='${pessoa.email}'/>" required>
                </div>

                <button class="btn btn-aviso" type="submit">Salvar alteracoes</button>
                <a class="btn btn-secundario" href="${pageContext.request.contextPath}/pessoas">Cancelar</a>
            </form>
        </c:when>
        <c:otherwise>
            <div class="alert alert-erro">Registro nao encontrado.</div>
            <a class="btn btn-secundario" href="${pageContext.request.contextPath}/pessoas">Voltar a lista</a>
        </c:otherwise>
    </c:choose>
</div>

<%@ include file="/WEB-INF/views/layout/footer.jspf" %>
