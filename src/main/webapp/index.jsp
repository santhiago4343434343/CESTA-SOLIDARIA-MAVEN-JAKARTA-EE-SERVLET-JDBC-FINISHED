<%@ page contentType="text/html;charset=UTF-8" %>
    <%@ taglib prefix="c" uri="jakarta.tags.core" %>
        <c:set var="tituloPagina" value="Cesta Solidaria - Inicio" />
        <%@ include file="/WEB-INF/views/layout/header.jspf" %>

            <div class="card">
                <h1>Cesta Solidaria 🧺</h1>
                <p class="texto-suave">
                    Cadastre-se para entrar na lista de retirada da <strong>cesta basica</strong>.
                    Assim que voce se cadastra, enviamos um <strong>e-mail de confirmacao</strong>.
                    Um projeto didatico em Jakarta EE (Servlet + JSP), com login, envio de e-mail
                    e API REST.
                </p>

                <div class="grade-links">
                    <a class="btn" href="${pageContext.request.contextPath}/pessoas?acao=novo">Fazer meu cadastro</a>
                    <a class="btn btn-secundario" href="${pageContext.request.contextPath}/login">Entrar</a>
                    <a class="btn btn-secundario" href="${pageContext.request.contextPath}/pessoas">Ver inscritos</a>
                </div>
            </div>

            <%--=====Secao tematica em Tailwind: o conteudo da cesta=====Toda estilizada com classes utilitarias do
                Tailwind (grid, rounded-lg, shadow-sm...), enquanto o cartao acima usa as classes do SCSS. --%>
                <section class="mt-6">
                    <h2 class="text-xl font-bold text-slate-800">O que vem na cesta 🧺</h2>
                    <p class="mt-1 text-sm text-slate-500">Itens basicos entregues a cada familia cadastrada.</p>

                    <div class="mt-4 grid grid-cols-2 gap-3 sm:grid-cols-3 md:grid-cols-4">
                        <div class="flex items-center gap-3 rounded-lg border border-slate-200 bg-white p-3 shadow-sm">
                            <span class="text-2xl">🍚</span><span
                                class="text-sm font-medium text-slate-700">Arroz</span>
                        </div>
                        <div class="flex items-center gap-3 rounded-lg border border-slate-200 bg-white p-3 shadow-sm">
                            <span class="text-2xl">🫘</span><span
                                class="text-sm font-medium text-slate-700">Feijao</span>
                        </div>
                        <div class="flex items-center gap-3 rounded-lg border border-slate-200 bg-white p-3 shadow-sm">
                            <span class="text-2xl">🛢️</span><span
                                class="text-sm font-medium text-slate-700">Oleo</span>
                        </div>
                        <div class="flex items-center gap-3 rounded-lg border border-slate-200 bg-white p-3 shadow-sm">
                            <span class="text-2xl">🍝</span><span
                                class="text-sm font-medium text-slate-700">Macarrao</span>
                        </div>
                        <div class="flex items-center gap-3 rounded-lg border border-slate-200 bg-white p-3 shadow-sm">
                            <span class="text-2xl">☕</span><span class="text-sm font-medium text-slate-700">Cafe</span>
                        </div>
                        <div class="flex items-center gap-3 rounded-lg border border-slate-200 bg-white p-3 shadow-sm">
                            <span class="text-2xl">🧂</span><span class="text-sm font-medium text-slate-700">Sal</span>
                        </div>
                        <div class="flex items-center gap-3 rounded-lg border border-slate-200 bg-white p-3 shadow-sm">
                            <span class="text-2xl">🥫</span><span
                                class="text-sm font-medium text-slate-700">Enlatados</span>
                        </div>
                        <div class="flex items-center gap-3 rounded-lg border border-slate-200 bg-white p-3 shadow-sm">
                            <span class="text-2xl">🍬</span><span
                                class="text-sm font-medium text-slate-700">Acucar</span>
                        </div>
                        <div class="flex items-center gap-3 rounded-lg border border-slate-200 bg-white p-3 shadow-sm">
                            <span class="text-2xl">🍞</span><span class="text-sm font-medium text-slate-700">Pães</span>
                        </div>
                        <div class="flex items-center gap-3 rounded-lg border border-slate-200 bg-white p-3 shadow-sm">
                            <span class="text-2xl">🍪</span><span
                                class="text-sm font-medium text-slate-700">Biscoitos</span>
                        </div>
                        <div class="flex items-center gap-3 rounded-lg border border-slate-200 bg-white p-3 shadow-sm">
                            <span class="text-2xl">🍎</span><span
                                class="text-sm font-medium text-slate-700">Frutas</span>
                        </div>
                        <div class="flex items-center gap-3 rounded-lg border border-slate-200 bg-white p-3 shadow-sm">
                            <span class="text-2xl">🥛</span><span
                                class="text-sm font-medium text-slate-700">Laticinios</span>
                        </div>
                    </div>
                </section>

                <%@ include file="/WEB-INF/views/layout/footer.jspf" %>