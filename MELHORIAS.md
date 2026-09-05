# Melhorias aplicadas — guia de estudo

Este documento explica **o que mudou**, **por quê**, e **o conceito** por trás
de cada mudança. Serve como roteiro de estudo do projeto.

---

## 1. Apresentação (frontend)

### 1.1 Fim do `<iframe>` → layout compartilhado
- **Antes:** `index.html` usava um `<iframe>` e um menu "dropdown" cujo CSS nem
  existia (o menu não abria). Clicar em "Início" carregava uma página dentro da
  outra.
- **Agora:** `index.jsp` + um cabeçalho/rodapé compartilhados
  (`WEB-INF/views/layout/header.jspf` e `footer.jspf`), incluídos em toda tela
  com `<%@ include %>`.
- **Conceito:** *template/layout* por composição de JSP. Um único lugar define a
  navbar, o `<head>`, o CSS. Menos repetição, visual consistente.

### 1.2 CSS único
- **Antes:** cada JSP tinha seu próprio `<style>` inline (cores diferentes, tudo
  repetido).
- **Agora:** um só arquivo `css/styles.css`, linkado no header.
- **Conceito:** separação de responsabilidades; manutenção num ponto só.

### 1.3 Proteção contra XSS (`<c:out>`)
- **Antes:** `${p.nomePessoa}` era impresso "cru". Um nome com `<script>` seria
  executado no navegador (Cross-Site Scripting armazenado).
- **Agora:** `<c:out value="${p.nomePessoa}"/>` — o JSTL **escapa** o HTML.
- **Conceito:** *output encoding*. Em JSP, EL não escapa sozinho; `c:out` sim.

### 1.4 Acessibilidade e feedback
- Labels ligados aos campos (`<label for="nome">` + `id="nome"`).
- Mensagens **flash** de sucesso/erro após cada ação.

---

## 2. Backend (Java)

### 2.1 Exclusão via POST (antes era GET)
- **Antes:** excluir era um link `?acao=excluir` (método GET). Um *prefetch* do
  navegador ou um robô poderia apagar registros só de "visitar" o link.
- **Agora:** um `<form method="post">` com um botão.
- **Conceito:** métodos HTTP têm semântica. GET = ler (seguro/idempotente),
  POST = alterar estado. Nunca destrua dados num GET.

### 2.2 Padrão PRG (Post/Redirect/Get) + flash
- Depois de salvar/editar/excluir, o servlet faz `sendRedirect` e guarda a
  mensagem na **sessão**. A tela seguinte exibe e apaga a mensagem.
- **Conceito:** evita o "reenviar formulário?" ao dar F5 e mostra feedback.

### 2.3 Busca + paginação
- O `PessoaDAO.listar(filtro, offset, limit)` usa `LIKE` + `LIMIT/OFFSET`; o
  `contar(filtro)` calcula o total de páginas.
- **Conceito:** não trazer a tabela inteira para a memória; paginar no banco.

### 2.4 Login por sessão (novo!)
- **Antes:** o projeto já sabia gerar/verificar hash BCrypt, mas
  `verificarSenha` só era usado nos **testes** — não havia login.
- **Agora:** `LoginServlet` confere e-mail + senha (`buscarPorEmail` +
  `verificarSenha`) e guarda o usuário na sessão. `AutenticacaoFilter` protege a
  área `/pessoas`. Cadastro e login ficam públicos.
- **Conceitos:** `HttpSession`, `Filter` (interceptador antes do servlet),
  autenticação, e boas práticas: mensagem de erro genérica e regeneração de
  sessão (contra *session fixation*).

### 2.5 Credenciais por variável de ambiente
- `Conexao.java` lê `DB_URL`/`DB_USER`/`DB_PASSWORD` (com padrão local).
- **Conceito:** configuração fora do código; o mesmo `.war` roda em qualquer
  ambiente (12-Factor App).

### 2.6 REST funcional (Jersey)
- **Antes:** havia `HelloResource` (JAX-RS), mas o Tomcat **não traz** JAX-RS,
  então o `/api` não funcionava de fato.
- **Agora:** o **Jersey** foi adicionado ao `pom.xml` e empacotado no `.war`.
  Novo `PessoaResource` devolve JSON. A senha nunca sai (`@JsonbTransient`).
- **Conceito:** Tomcat = container de Servlet; recursos "provided" precisam de
  uma implementação real quando o servidor não os oferece.

---

## 3. Como uma requisição percorre o sistema

Exemplo: abrir a lista de pessoas logado.

```
GET /pessoas
  → AutenticacaoFilter  (está logado? sim → segue)
  → PessoaServlet.doGet  (acao ausente → listar)
      → PessoaDAO.contar()  e  PessoaDAO.listar(busca, offset, limite)
          → Conexao.getConnection()  (empresta do pool HikariCP)
          → PreparedStatement no MySQL
  → req.setAttribute("pessoas", ...)  → forward para listar.jsp
  → listar.jsp (JSTL) monta a tabela, escapando os dados com c:out
```

---

## 4. Glossário rápido

- **MVC:** Model (dados), View (tela), Controller (orquestra).
- **DAO:** classe que isola o acesso ao banco.
- **JSTL/EL:** tags (`c:forEach`, `c:if`) + `${...}` para não escrever Java no JSP.
- **PreparedStatement:** consulta parametrizada; barra SQL Injection.
- **Pool de conexões:** reaproveita conexões abertas (mais rápido).
- **BCrypt:** hash lento e com *salt* para senhas.
- **Filter:** intercepta requisições antes do servlet (aqui, para autenticar).
- **PRG:** Post → Redirect → Get, para feedback e F5 seguro.

---

## 5. Próximos passos sugeridos (para continuar estudando)

1. Validar e-mail único (índice UNIQUE + tratamento do erro).
2. Trocar `System.out` por um logger (SLF4J/Logback).
3. Criar testes do DAO com um banco em memória (H2/Testcontainers).
4. Adicionar CDI de verdade (Weld) e injetar o DAO com `@Inject`.
5. Completar o CRUD REST (POST/PUT/DELETE em `/api/pessoas`).
