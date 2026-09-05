<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<c:set var="tituloPagina" value="Lista de Inscritos"/>
<%@ include file="/WEB-INF/views/layout/header.jspf" %>

<div class="barra-topo">
    <h2>Inscritos na cesta</h2>
    <a class="btn" href="${pageContext.request.contextPath}/pessoas?acao=novo">+ Novo inscrito</a>
</div>

<%-- Busca: envia por GET para /pessoas?busca=...  (volta sempre para a pagina 1) --%>
<form class="busca" method="get" action="${pageContext.request.contextPath}/pessoas">
    <input type="text" name="busca" value="<c:out value='${busca}'/>"
           placeholder="Buscar por nome ou e-mail...">
    <button class="btn btn-secundario" type="submit">Buscar</button>
    <c:if test="${not empty busca}">
        <a class="btn btn-secundario" href="${pageContext.request.contextPath}/pessoas">Limpar</a>
    </c:if>
</form>

<div class="tabela-wrap mt">
    <table>
        <thead>
        <tr>
            <th>ID</th>
            <th>Nome</th>
            <th>E-mail</th>
            <th>Papel</th>
            <th>Status</th>
            <th>Acoes</th>
        </tr>
        </thead>
        <tbody>
        <%-- c:out ESCAPA o HTML: protege contra XSS caso alguem cadastre
             um nome como <script>...  Antes usavamos ${p.nomePessoa} cru. --%>
        <c:forEach var="p" items="${pessoas}">
            <tr>
                <td><c:out value="${p.idPessoa}"/></td>
                <td><c:out value="${p.nomePessoa}"/></td>
                <td><c:out value="${p.email}"/></td>
                <td>
                    <c:choose>
                        <c:when test="${p.papel == 'ADMIN'}">Administrador</c:when>
                        <c:otherwise>Beneficiario</c:otherwise>
                    </c:choose>
                </td>
                <td>
                    <%-- Selo colorido conforme o status --%>
                    <c:choose>
                        <c:when test="${p.status == 'retirado'}">
                            <span class="selo selo-retirado">Retirado</span>
                        </c:when>
                        <c:otherwise>
                            <span class="selo selo-aguardando">Aguardando</span>
                        </c:otherwise>
                    </c:choose>
                </td>
                <td>
                    <div class="acoes">
                        <%-- Alternar status (POST) --%>
                        <form method="post" action="${pageContext.request.contextPath}/pessoas" style="margin:0;">
                            <input type="hidden" name="acao" value="status">
                            <input type="hidden" name="id" value="${p.idPessoa}">
                            <c:choose>
                                <c:when test="${p.status == 'retirado'}">
                                    <input type="hidden" name="novo" value="aguardando">
                                    <button class="btn btn-secundario btn-pequeno" type="submit">Reabrir</button>
                                </c:when>
                                <c:otherwise>
                                    <input type="hidden" name="novo" value="retirado">
                                    <button class="btn btn-sucesso btn-pequeno" type="submit">Retirar</button>
                                </c:otherwise>
                            </c:choose>
                        </form>

                        <a class="btn btn-secundario btn-pequeno"
                           href="${pageContext.request.contextPath}/pessoas?acao=detalhes&amp;id=${p.idPessoa}">Detalhes</a>
                        <a class="btn btn-aviso btn-pequeno"
                           href="${pageContext.request.contextPath}/pessoas?acao=editar&amp;id=${p.idPessoa}">Editar</a>

                        <%-- Exclusao via POST (formulario), nao via link GET:
                             assim um prefetch/crawler nao apaga registros. --%>
                        <form method="post" action="${pageContext.request.contextPath}/pessoas"
                              onsubmit="return confirm('Excluir ${fn:escapeXml(p.nomePessoa)}?');"
                              style="margin:0;">
                            <input type="hidden" name="acao" value="excluir">
                            <input type="hidden" name="id" value="${p.idPessoa}">
                            <button class="btn btn-perigo btn-pequeno" type="submit">Excluir</button>
                        </form>
                    </div>
                </td>
            </tr>
        </c:forEach>

        <c:if test="${empty pessoas}">
            <tr>
                <td colspan="6" class="vazio">
                    <c:choose>
                        <c:when test="${not empty busca}">Nenhum resultado para "<c:out value='${busca}'/>".</c:when>
                        <c:otherwise>Nenhum inscrito ainda.</c:otherwise>
                    </c:choose>
                </td>
            </tr>
        </c:if>
        </tbody>
    </table>
</div>

<%-- Paginacao: so aparece se houver mais de uma pagina --%>
<c:if test="${totalPaginas > 1}">
    <div class="paginacao">
        <c:forEach var="i" begin="1" end="${totalPaginas}">
            <c:choose>
                <c:when test="${i == pagina}">
                    <span class="atual">${i}</span>
                </c:when>
                <c:otherwise>
                    <a href="${pageContext.request.contextPath}/pessoas?pagina=${i}&amp;busca=${fn:escapeXml(busca)}">${i}</a>
                </c:otherwise>
            </c:choose>
        </c:forEach>
    </div>
</c:if>

<%@ include file="/WEB-INF/views/layout/footer.jspf" %>

