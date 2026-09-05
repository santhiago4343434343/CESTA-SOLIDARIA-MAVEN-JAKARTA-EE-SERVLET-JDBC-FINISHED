# Estudo técnico aprofundado — Cesta Solidária

Material de estudo dos **pontos principais** do projeto: arquitetura, cada
tecnologia por baixo dos panos, padrões de projeto, segurança e o caminho
completo de uma requisição. Serve para você entender *por que* cada peça existe
e conseguir explicar em uma entrevista ou apresentação.

---

## 1. Visão geral: MVC em camadas

O projeto segue **MVC** com um **Front Controller** e a camada **DAO**:

```
             (HTTP)                          (JDBC)
Navegador  ─────────►  Filtro ─► Servlet ─► DAO ─► Pool ─► MySQL
    ▲                  (auth)   (Controller)      (Hikari)
    │                              │
    └──────────  JSP (View)  ◄─────┘  o Servlet escolhe a View e injeta os dados
```

- **Model** — `classes/Pessoa` (dados + regras de senha).
- **View** — `WEB-INF/views/*.jsp` (só exibem; ficam em `WEB-INF` para não
  serem acessadas direto pela URL).
- **Controller** — `PessoaServlet` / `LoginServlet` (recebem a requisição,
  orquestram, encaminham para a View).
- **DAO** — `PessoaDAO` (todo o SQL isolado; se um dia trocar de banco, muda só
  aqui).
- **Infra** — `Conexao` (pool), `AutenticacaoFilter`, `EmailService`.

**Por que separar assim?** Cada camada tem uma responsabilidade única (SRP). O
JSP nunca fala com o banco; o DAO nunca sabe o que é HTTP. Isso deixa o código
testável e fácil de manter.

---

## 2. O núcleo web: Servlet + JSP + JSTL/EL

### Servlet e o padrão Front Controller
`PessoaServlet` está mapeado em `@WebServlet("/pessoas")` e concentra todas as
operações através de um parâmetro `acao` (`novo`, `editar`, `salvar`, `excluir`,
`status`...). Isso é o padrão **Front Controller**: uma única porta de entrada
que roteia para a lógica certa, em vez de um servlet por operação.

### `forward` vs `redirect` (conceito central)
- **`forward`** (`RequestDispatcher.forward`) — encaminhamento **interno** no
  servidor. A URL do navegador **não muda**, e os `request.setAttribute(...)`
  sobrevivem. Usado para **mostrar** uma tela (ex.: listar → `listar.jsp`).
- **`redirect`** (`response.sendRedirect`) — manda o navegador **fazer outra
  requisição** (a URL muda). Os atributos de request **se perdem**. Usado
  **depois de gravar** algo (ver PRG, seção 6).

### JSP + JSTL + EL (telas sem Java)
- **EL** (Expression Language): `${pessoa.nomePessoa}` chama `getNomePessoa()`.
- **JSTL**: tags como `<c:forEach>`, `<c:if>`, `<c:choose>` — lógica de exibição
  sem escrever Java no JSP (nada de scriptlet `<% ... %>`).
- **Ponto de segurança:** EL **não escapa HTML** sozinho. Por isso usamos
  `<c:out value="${...}"/>`, que escapa. (Ver seção 12.)

### Layout por composição
`header.jspf` e `footer.jspf` são incluídos com `<%@ include %>` (include
**estático**, resolvido na tradução do JSP). Assim todas as telas compartilham
navbar, `<head>` e CSS num lugar só.

---

## 3. Acesso a dados: JDBC + PreparedStatement + pool

### JDBC e PreparedStatement
Todo SQL usa `PreparedStatement` com marcadores `?`:

```java
String sql = "SELECT ... FROM pessoa WHERE idpessoa = ?";
stm.setInt(1, id);
```

O driver envia a **query** e os **valores** separadamente. O valor nunca é
"colado" no texto do SQL — é isso que **elimina SQL Injection** (um valor como
`1; DROP TABLE pessoa` vira só um dado, nunca comando).

### Pool de conexões (HikariCP)
Abrir conexão com banco é **caro** (handshake TCP, autenticação). O **pool**
mantém um conjunto de conexões abertas e as **empresta**:

```java
try (Connection con = Conexao.getConnection()) { ... }  // close() DEVOLVE ao pool
```

`Conexao` cria **um** `HikariDataSource` no bloco `static` (uma vez, quando a
classe carrega) e reusa. O `try-with-resources` chama `close()`, que **não
fecha de verdade** — devolve a conexão ao pool. `setMaximumPoolSize(10)` limita
a 10 conexões simultâneas.

---

## 4. Segurança de senha: BCrypt

Senha **nunca** é guardada em texto puro. Guardamos o **hash BCrypt**:

```java
BCrypt.hashpw(senha, BCrypt.gensalt());       // gera hash com salt aleatorio
BCrypt.checkpw(senhaDigitada, hashArmazenado); // confere no login
```

Três ideias importantes:

1. **Salt aleatório** — cada hash embute um "sal" único. Por isso **duas pessoas
   com a mesma senha têm hashes diferentes** (o teste `PessoaTest` prova isso), e
   *rainbow tables* não funcionam.
2. **Custo (work factor)** — BCrypt é **propositalmente lento** (por padrão 2^10
   rodadas). Isso não incomoda no login, mas torna ataques de força bruta
   caríssimos. O custo pode subir com o tempo (hardware mais rápido).
3. **Formato** — o hash (`$2a$10$....`) carrega versão + custo + salt + digest,
   então o `checkpw` sabe se re-derivar sem guardar o salt à parte.

A coluna `senha` guarda 60 caracteres (tamanho do hash BCrypt).

---

## 5. Sessão e autenticação (Filter + HttpSession)

### HttpSession
HTTP é **sem estado**. Para "lembrar" que alguém logou, usamos a **sessão**: o
servidor guarda dados e manda um cookie `JSESSIONID`; o navegador reenvia esse
cookie a cada requisição. No login:

```java
sessao.setAttribute("usuarioLogado", pessoa.getNomePessoa());
```

### Filter (interceptador)
`AutenticacaoFilter` (`@WebFilter("/pessoas")`) roda **antes** do servlet. Se o
usuário não está logado (e a ação não é pública, como cadastro), ele redireciona
para `/login`. É o padrão **Intercepting Filter**: a regra de acesso fica num só
lugar, fora da lógica de negócio.

### Proteção contra *session fixation*
No login bem-sucedido, **invalidamos a sessão antiga e criamos uma nova**
(`invalidate()` + `getSession(true)`). Isso troca o `JSESSIONID`, impedindo que
um atacante que tenha "plantado" um id de sessão conhecido aproveite o login da
vítima. Também usamos **mensagem de erro genérica** ("e-mail ou senha inválidos")
para não revelar se um e-mail existe.

---

## 6. PRG (Post/Redirect/Get) + mensagens flash

Depois de um **POST** que grava (salvar, excluir, mudar status), o servlet faz
**redirect** em vez de responder direto. Fluxo: `POST → 302 redirect → GET`.

**Por quê?** Se respondêssemos direto ao POST, apertar **F5** reenviaria o
formulário (cadastrar/excluir de novo). Com o redirect, o F5 só repete o GET
(inofensivo).

Como a mensagem de sucesso precisa sobreviver ao redirect (que perde os
atributos de request), guardamos ela na **sessão** ("flash"):

```java
sessao.setAttribute("msg", "Pessoa excluida.");   // no POST
// o header.jspf exibe e depois REMOVE com <c:remove>
```

---

## 7. Máquina de estados do `status` (aguardando ↔ retirado)

O `status` é um campo de texto com dois estados válidos:

```
  [aguardando]  ── botão "Retirar" (POST) ──►  [retirado]
       ▲                                            │
       └──────────  botão "Reabrir" (POST) ─────────┘
```

Pontos técnicos:

- **Banco:** coluna `status VARCHAR(20) NOT NULL DEFAULT 'aguardando'`. Para
  tabelas já existentes, há um `ALTER TABLE` no `script.sql`.
- **Validação no servidor:** o controlador só aceita `"aguardando"` ou
  `"retirado"` (`alterarStatus`). Nunca confie no valor que veio do formulário —
  o cliente pode mandar qualquer coisa.
- **View:** um **selo** colorido (`.selo-aguardando` âmbar / `.selo-retirado`
  verde) mostra o estado; o botão muda de "Retirar" para "Reabrir" conforme o
  estado atual (`<c:choose>`).
- **Mutação por POST**, seguindo o PRG.

---

## 8. E-mail assíncrono (Jakarta Mail + Mailtrap)

### SMTP e STARTTLS
Enviar e-mail é falar **SMTP** com um servidor. O Mailtrap dá um servidor
**sandbox** (não entrega de verdade, só mostra numa caixa de teste). Usamos
`mail.smtp.auth=true` (login) e `mail.smtp.starttls.enable=true` (**STARTTLS**:
começa em texto e "sobe" para TLS criptografado na mesma conexão, porta 2525).

### API (Jakarta Mail)
`Session` (com um `Authenticator` para usuário/senha) → `MimeMessage`
(remetente, destinatário, assunto, corpo HTML) → `Transport.send(msg)`.

### Por que assíncrono?
Falar com um servidor SMTP pode levar segundos. Se enviássemos **dentro** da
requisição de cadastro, o usuário ficaria esperando. Então usamos um
`ExecutorService` de **uma thread daemon**:

```java
POOL.submit(() -> { ... enviar ... });  // cadastro responde na hora
```

E envolvemos em `try/catch`: se o e-mail falhar, **loga e segue** — nunca quebra
o cadastro. Se as credenciais não estiverem configuradas, o envio é só pulado.
O Tomcat não traz implementação de e-mail, então empacotamos o **Angus Mail** no
`.war`.

---

## 9. REST com JAX-RS / Jersey

### Por que o Tomcat precisa do Jersey
Tomcat é um **container de Servlets** — ele **não** implementa JAX-RS. A
dependência da API vem marcada `provided` (esperando que "alguém" forneça). Em
um servidor Jakarta EE completo (WildFly, Payara) isso viria pronto; no Tomcat,
não. Por isso empacotamos o **Jersey** (implementação de referência do JAX-RS)
dentro do `.war`.

### Como funciona
- `HelloApplication` com `@ApplicationPath("/api")` registra o JAX-RS.
- `PessoaResource` com `@Path("/pessoas")` e `@GET @Produces(APPLICATION_JSON)`
  devolve `List<Pessoa>`.
- O **JSON-B** (Yasson, via `jersey-media-json-binding`) serializa o objeto em
  JSON automaticamente.
- **`@JsonbTransient`** no `getSenha()` garante que a senha **nunca** apareça no
  JSON.

Resultado: `GET /api/pessoas` retorna a lista em JSON, sem senha.

---

## 10. Build do frontend: Sass + Tailwind

O `styles.css` **não é escrito à mão** — é **gerado**:

```
styles.scss ──(Dart Sass)──► compiled.css ──(Tailwind/PostCSS)──► styles.css
```

- **Sass** processa `$variáveis`, **aninhamento** e `@mixin` (ex.: o mixin
  `botao(...)` gera `.btn`, `.btn-perigo`, `.btn-sucesso`...), e **deixa passar**
  as diretivas `@tailwind`.
- **Tailwind** substitui `@tailwind base/components/utilities`, **lê os arquivos
  `.jsp`** (config `content`) para descobrir quais utilitárias você usou e gera
  só essas (*tree-shaking*), minificando no fim.
- **Preflight**: o `@tailwind base` é um **reset** que zera margens, títulos,
  listas. Por isso o SCSS redefine `h1/h2` explicitamente.
- Convivência: classes de **componente** (`.navbar`, `.card`) vêm do Sass;
  classes **utilitárias** (`flex`, `bg-sky-50`) vêm do Tailwind — no mesmo
  arquivo final. O navegador só vê **CSS puro**, por isso funciona 100% com
  Jakarta/JSP.

---

## 11. Maven: ciclo de vida, escopos e empacotamento

- **Ciclo de vida**: `validate → compile → test → package → ...`. `mvn package`
  compila, **roda os testes** (JUnit) e gera o artefato.
- **`packaging = war`**: gera um **WAR** (Web ARchive) — o `.zip` que o Tomcat
  entende (classes em `WEB-INF/classes`, libs em `WEB-INF/lib`).
- **Escopos de dependência** (ponto muito cobrado):
  - `provided` — precisa para **compilar**, mas **não** vai no WAR porque o
    **servidor fornece** (ex.: `jakarta.servlet-api` — o Tomcat já tem).
  - `compile` (padrão) — vai **dentro** do WAR (ex.: HikariCP, Jersey, jBCrypt,
    Angus Mail — o Tomcat **não** fornece).
  - `test` — só nos testes (JUnit).
- **Cargo plugin**: baixa o Tomcat 10.1 e sobe a aplicação (`mvn cargo:run`),
  sem precisar do IntelliJ Ultimate nem instalar o Tomcat à parte.

---

## 12. Segurança — resumo dos mecanismos

| Ameaça | Defesa no projeto |
|---|---|
| SQL Injection | `PreparedStatement` com `?` (nunca concatena valor no SQL) |
| XSS (script no nome) | `<c:out>` escapa a saída nas telas |
| Senha vazada no banco | Hash **BCrypt** com salt (nunca texto puro) |
| Apagar/alterar por link (GET) | Mutações são **POST** (excluir, status) |
| Session fixation | Sessão **regenerada** no login |
| Enumeração de e-mail | Erro de login **genérico** |
| Senha vazar na API | `@JsonbTransient` no `getSenha()` |
| Credenciais no código | Config por **variável de ambiente** (banco e e-mail) |

---

## 13. Fluxo end-to-end: um cadastro completo

```
1. POST /pessoas  (acao=salvar, nome, email, senha, confirmaSenha)
2. AutenticacaoFilter  → acao "salvar" é publica → deixa passar
3. PessoaServlet.doPost → salvar():
     - valida (campos preenchidos, senhas iguais) NO SERVIDOR
     - Pessoa nova (status = "aguardando")
     - PessoaDAO.inserir():
         · Pessoa.gerarHash(senha)  → BCrypt
         · Conexao.getConnection()  → empresta do pool Hikari
         · PreparedStatement INSERT
     - EmailService.enviarConfirmacaoAsync()  → thread separada envia SMTP
     - flash("Conta criada!")  → sessao
     - sendRedirect(/login)     → PRG
4. GET /login  → LoginServlet → login.jsp → header.jspf mostra o flash
```

Repare quantos conceitos das seções acima aparecem **numa única ação**:
validação, hashing, pool, SQL parametrizado, envio assíncrono, PRG e flash.

---

## 14. Padrões de projeto usados

- **MVC** — separação Model / View / Controller.
- **Front Controller** — um servlet roteando por `acao`.
- **DAO** — isolamento do acesso a dados.
- **Intercepting Filter** — o filtro de autenticação.
- **Object Pool** — o pool de conexões (HikariCP).
- **PRG (Post/Redirect/Get)** — mutações seguidas de redirect.
- **Producer/Worker assíncrono** — a fila de e-mail (ExecutorService).

---

## 15. Concorrência (importante e sutil)

- Um **servlet é multithread**: existe **uma** instância e várias threads a
  atendem ao mesmo tempo. Logo, **não** guarde estado mutável por requisição em
  campo do servlet. O `PessoaServlet` só tem um `PessoaDAO` **sem estado** (só
  métodos), então é seguro.
- O **DAO** abre e fecha conexão dentro de cada método (try-with-resources); não
  compartilha `Connection` entre threads.
- O **pool** Hikari é *thread-safe* (empresta conexões diferentes a cada thread).
- O **executor de e-mail** processa os envios numa fila, sem travar as threads
  das requisições.

---

## 16. Glossário rápido

- **WAR** — pacote da aplicação web que o Tomcat implanta.
- **Container / Servlet Container** — o Tomcat: executa Servlets/JSP.
- **EL / JSTL** — expressões `${}` e tags `<c:...>` nos JSP.
- **Pool de conexões** — conjunto reutilizável de conexões com o banco.
- **Hash / Salt** — resumo irreversível da senha / valor aleatório por senha.
- **Sessão** — memória por usuário no servidor, ligada ao cookie JSESSIONID.
- **Filter** — interceptador antes do servlet.
- **PRG** — Post → Redirect → Get.
- **STARTTLS** — upgrade de uma conexão SMTP para TLS.
- **Preflight (Tailwind)** — reset de CSS base do Tailwind.
- **Scope provided/compile** — dependência fornecida pelo servidor / empacotada.

---

## 17. Perguntas para fixar (teste-se)

1. Por que os JSP ficam em `WEB-INF/views` e não direto em `webapp`?
2. Qual a diferença entre `forward` e `redirect`, e quando usar cada um?
3. Como o `PreparedStatement` evita SQL Injection?
4. Por que dois hashes da mesma senha são diferentes?
5. O que o `close()` de uma conexão do HikariCP realmente faz?
6. Por que a exclusão é POST e não um link GET?
7. Por que o Tomcat precisa do Jersey empacotado, mas não do `jakarta.servlet-api`?
8. Por que o e-mail é enviado numa thread separada?
9. O que muda no `styles.css` se você adicionar uma classe Tailwind nova num JSP?
10. Por que regenerar a sessão no login?

> Dica: consegue responder as 10 sem olhar? Então você entende os pontos
> principais do projeto. As respostas estão espalhadas nas seções acima.
