# Este projeto está licenciado sob CC BY-NC-ND 4.0 — permitido visualizar e compartilhar para fins de estudo, proibido uso comercial e criação de obras derivadas



# NewProject004 — CRUD de Pessoas (Jakarta EE + Tomcat)

Aplicação web didática em **Jakarta EE** que demonstra um CRUD completo
(Cadastrar, Consultar, Editar e Excluir) de pessoas, com **login por sessão**,
**busca**, **paginação** e um **endpoint REST** de exemplo.

As telas são feitas em **JSP + JSTL** (sem código Java misturado), o acesso ao
banco é via **JDBC** com **pool de conexões (HikariCP)**, e as senhas são
guardadas com **hash BCrypt**.

## Arquitetura (MVC em camadas)

```
Navegador
   │
   ▼
AutenticacaoFilter  ──►  PessoaServlet (Controller)
                              │  fala só com o DAO
                              ▼
                         PessoaDAO (acesso a dados / JDBC)
                              │
                              ▼
                         Conexao (pool HikariCP) ──► MySQL
   ▲
   │  o Servlet escolhe a view e passa os dados
   └────────────  JSP + JSTL (View, só exibe)
```

- **Model:** `classes/Pessoa.java` (POJO + helpers de senha BCrypt)
- **DAO:** `dao/PessoaDAO.java` (todo o SQL/JDBC, com `PreparedStatement`)
- **Controller:** `com/example/newproject004/PessoaServlet.java`
- **View:** `WEB-INF/views/*.jsp` (protegidas: o usuário não abre o JSP direto)
- **Infra:** `utils/Conexao.java` (pool), `AutenticacaoFilter`, `LoginServlet`
- **REST:** `PessoaResource` + `HelloResource` (servidos pelo Jersey em `/api`)

## Tecnologias

| Tecnologia | Versão | Para quê |
|---|---|---|
| Java | 21 (LTS) | Linguagem base |
| Jakarta EE | 10 | Plataforma (Servlet, JSP, JSTL, CDI, JAX-RS) |
| Jakarta Servlet | 6.0 | Requisições HTTP (o Controller) |
| JSP + JSTL | 3.0 | Telas HTML no servidor, sem scriptlet |
| Maven | — | Dependências e empacotamento `.war` |
| Apache Tomcat | 10.1 | Servidor/container web |
| Cargo (plugin) | 1.10.28 | Baixa e sobe o Tomcat pelo Maven |
| MySQL / MariaDB | — | Banco de dados relacional |
| MySQL Connector/J | 8.3 | Driver JDBC |
| HikariCP | 5.1 | Pool de conexões |
| jBCrypt | 0.4 | Hash de senha (BCrypt) |
| Jersey (JAX-RS) | 3.1 | Implementação REST (JSON) |
| JUnit | 5.10 | Testes de unidade |
| Sass (Dart Sass) | 1.x | Estilos com variáveis, aninhamento e mixins |
| Tailwind CSS | 3.4 | Classes utilitárias direto no HTML |
| Jakarta Mail (Angus) | 2.0 | Envio do e-mail de confirmação (Mailtrap) |

## Funcionalidades

- **Cadastro** de pessoa com confirmação de senha (hash BCrypt).
- **E-mail de confirmação** disparado a cada cadastro (via Mailtrap).
- **Login/Logout** por sessão (usa o BCrypt para conferir a senha).
- **Listagem** com **busca** (nome ou e-mail) e **paginação**.
- **Detalhes**, **Edição** e **Exclusão** (exclusão via POST, não via link).
- **Status na fila** (aguardando / retirado) com selo colorido e botão de alternar.
- **Mensagens de feedback** (flash) após cada ação.
- **API REST** em JSON: `GET /api/pessoas` e `GET /api/pessoas/{id}`.

> 📚 **Material de estudo:** veja `ESTUDO-TECNICO.md` (aprofundado) e
> `MELHORIAS.md` (o antes/depois das melhorias).

## Estrutura do projeto

```
NewProject004/
├── pom.xml
├── script.sql                                  # cria o banco e a tabela
├── frontend/                                   # build do CSS (Sass + Tailwind)
│   ├── src/styles.scss                         # FONTE dos estilos (edite aqui)
│   └── tailwind.config.js
└── src/
    ├── main/
    │   ├── java/
    │   │   ├── classes/Pessoa.java              # Model + BCrypt
    │   │   ├── dao/PessoaDAO.java               # JDBC (CRUD + busca/paginação)
    │   │   ├── utils/Conexao.java               # Pool HikariCP
    │   │   └── com/example/newproject004/
    │   │       ├── PessoaServlet.java           # Controller do CRUD
    │   │       ├── LoginServlet.java            # Login/Logout
    │   │       ├── AutenticacaoFilter.java      # Protege /pessoas
    │   │       ├── HelloApplication.java        # Config JAX-RS (/api)
    │   │       ├── HelloResource.java           # REST exemplo
    │   │       └── PessoaResource.java          # REST de Pessoa (JSON)
    │   ├── resources/META-INF/beans.xml         # habilita CDI (ver nota)
    │   └── webapp/
    │       ├── index.jsp                        # página inicial
    │       ├── css/styles.css                   # GERADO pelo frontend (Sass+Tailwind)
    │       └── WEB-INF/views/
    │           ├── layout/header.jspf           # cabeçalho compartilhado
    │           ├── layout/footer.jspf           # rodapé compartilhado
    │           ├── listar.jsp
    │           ├── cadastrar.jsp
    │           ├── editar.jsp
    │           ├── detalhes.jsp
    │           └── login.jsp
    └── test/java/classes/PessoaTest.java        # testes do BCrypt
```

## Pré-requisitos

- JDK 21
- MySQL ou MariaDB em execução
- (O Tomcat 10.1 é baixado automaticamente pelo Cargo)

## 1. Configurar o banco de dados

Execute o `script.sql` no seu MySQL/MariaDB (cria o banco `tomcat_database` e a
tabela `pessoa`):

```bash
mysql -u root -p < script.sql
```

## 2. Configurar a conexão (variáveis de ambiente)

As credenciais **não ficam mais chumbadas no código**. O `Conexao.java` lê as
variáveis de ambiente abaixo e, se não existirem, usa um padrão de
desenvolvimento (`localhost`, usuário `root`, senha vazia):

| Variável | Padrão |
|---|---|
| `DB_URL` | `jdbc:mysql://localhost:3306/tomcat_database` |
| `DB_USER` | `root` |
| `DB_PASSWORD` | *(vazio)* |

```bash
# Exemplo (Linux/macOS)
export DB_PASSWORD="minhasenha"
```

## 3. Executar

```bash
./mvnw package cargo:run      # Linux/macOS
mvnw.cmd package cargo:run    # Windows
```

Na primeira vez o Cargo baixa o Tomcat. Quando aparecer
`Press Ctrl-C to stop the container`, acesse:

- Aplicação: <http://localhost:8080/NewProject004/>
- API REST: <http://localhost:8080/NewProject004/api/pessoas>

Primeiro uso: clique em **Criar conta** (cadastro é público), depois **Entrar**.
A listagem e as edições exigem login.

## Estilos (Sass + Tailwind)

O CSS **não é escrito à mão** em `styles.css` — esse arquivo é **gerado**. A
fonte fica em `frontend/src/styles.scss` (Sass: variáveis, aninhamento, mixins)
combinado com o Tailwind (classes utilitárias no HTML dos JSP). Para mudar o
visual, edite o SCSS e rode o build:

```bash
cd frontend
npm install      # só na primeira vez
npm run build    # gera src/main/webapp/css/styles.css
```

Detalhes e integração opcional com o Maven: veja `frontend/README-FRONTEND.md`.

## E-mail de confirmação (Mailtrap)

A cada cadastro, a aplicação dispara um **e-mail de confirmação**. Para testar
sem enviar e-mails de verdade, usamos o **Mailtrap** (uma caixa de entrada
"fake" para desenvolvimento).

1. Crie uma conta grátis em <https://mailtrap.io> e abra um **inbox** (Email
   Testing → Inboxes).
2. Em *SMTP Settings*, copie o **usuário** e a **senha** do inbox.
3. Defina as variáveis de ambiente antes de subir a aplicação:

```bash
export MAIL_USER="seu_usuario_mailtrap"
export MAIL_PASSWORD="sua_senha_mailtrap"
# opcionais (já têm padrão):
# export MAIL_HOST="sandbox.smtp.mailtrap.io"
# export MAIL_PORT="2525"
# export MAIL_FROM="Cesta Solidaria <no-reply@cestasolidaria.local>"
```

Agora cadastre uma pessoa: o e-mail aparece no seu inbox do Mailtrap.

Detalhes de comportamento:

- O envio roda em **segundo plano** (uma thread separada), então o cadastro não
  fica lento esperando o e-mail.
- Se `MAIL_USER`/`MAIL_PASSWORD` **não** estiverem definidos, o envio é apenas
  pulado e a aplicação continua funcionando normalmente.
- Qualquer falha de envio é registrada no log, sem quebrar o cadastro.

## Segurança (projeto didático)

- Senhas com **hash BCrypt** (nunca em texto puro).
- Consultas com **PreparedStatement** (proteção contra SQL Injection).
- Saída escapada com **`<c:out>`** nos JSPs (proteção contra XSS).
- **Exclusão via POST** (um prefetch/crawler não apaga registros por link).
- Credenciais do banco por **variável de ambiente**.
- Login regenera a sessão (mitiga *session fixation*).

## Nota sobre CDI e JAX-RS no Tomcat

O Tomcat é um container de **Servlets**, não um servidor Jakarta EE completo:

- **JAX-RS** não vem embutido — por isso o **Jersey** é empacotado no `.war`
  (dependências no `pom.xml`). Assim o `/api` funciona.
- **CDI** também não vem embutido. O `beans.xml` deixa o projeto pronto para
  CDI, mas para usar `@Inject` seria preciso adicionar o **Weld**. Por isso o
  DAO é instanciado com `new` (simples e funcional). Fica como próximo passo.

## Licença

MIT. Veja `LICENSE`.
