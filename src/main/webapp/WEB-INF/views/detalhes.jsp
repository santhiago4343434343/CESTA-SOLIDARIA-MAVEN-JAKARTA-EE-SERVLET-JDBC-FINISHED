<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="tituloPagina" value="Detalhes da Pessoa"/>
<%@ include file="/WEB-INF/views/layout/header.jspf" %>

<div class="card">
    <h2>Detalhes</h2>

    <c:choose>
        <c:when test="${pessoa != null}">
            <table>
                <tr><th style="width:120px;">ID</th><td><c:out value="${pessoa.idPessoa}"/></td></tr>
                <tr><th>Nome</th><td><c:out value="${pessoa.nomePessoa}"/></td></tr>
                <tr><th>E-mail</th><td><c:out value="${pessoa.email}"/></td></tr>
                <tr>
                    <th>Status</th>
                    <td>
                        <c:choose>
                            <c:when test="${pessoa.status == 'retirado'}">
                                <span class="selo selo-retirado">Retirado</span>
                            </c:when>
                            <c:otherwise>
                                <span class="selo selo-aguardando">Aguardando</span>
                            </c:otherwise>
                        </c:choose>
                    </td>
                </tr>
            </table>

            <div class="grade-links">
                <a class="btn btn-aviso"
                   href="${pageContext.request.contextPath}/pessoas?acao=editar&id=${pessoa.idPessoa}">Editar</a>
                <a class="btn btn-secundario" href="${pageContext.request.contextPath}/pessoas">Voltar a lista</a>
            </div>
        </c:when>
        <c:otherwise>
            <div class="alert alert-erro">Registro nao encontrado.</div>
            <a class="btn btn-secundario" href="${pageContext.request.contextPath}/pessoas">Voltar a lista</a>
        </c:otherwise>
    </c:choose>
</div>

<%@ include file="/WEB-INF/views/layout/footer.jspf" %>
