# Frontend — build do CSS (Sass + Tailwind)

Esta pasta gera o arquivo `src/main/webapp/css/styles.css` que o Tomcat serve.
Ela usa **duas** ferramentas juntas:

- **Sass (SCSS)** → os estilos "de componente" (`.navbar`, `.btn`, `.card`...),
  com variáveis (`$cor-navbar`), aninhamento e `@mixin`.
- **Tailwind** → as classes utilitárias usadas direto no HTML dos JSP
  (`flex`, `bg-sky-50`, `rounded-lg`, `p-6`...).

O navegador só recebe **CSS puro** no fim — por isso funciona 100% com
Jakarta/JSP/Tomcat: eles nem sabem que o CSS veio de Sass/Tailwind.

## Como o build funciona

```
src/styles.scss  --(Dart Sass)-->  dist/compiled.css  --(Tailwind)-->  ../src/main/webapp/css/styles.css
```

1. O **Sass** compila variáveis/aninhamento/mixins e deixa passar as
   diretivas `@tailwind`.
2. O **Tailwind** lê os arquivos `.jsp`/`.jspf` (configurado em
   `tailwind.config.js` → `content`), descobre quais classes você usou e gera
   só o CSS necessário, já minificado.

## Pré-requisito

- Node.js 18+ (só para gerar o CSS; o Tomcat/Java não precisam do Node).

## Passo a passo

Na primeira vez, instale as dependências:

```bash
cd frontend
npm install
```

Gerar o CSS uma vez:

```bash
npm run build
```

Ou deixar recompilando sozinho enquanto você edita:

```bash
npm run watch
```

## Onde editar o quê

- **Estilo de componente / cores** → edite `frontend/src/styles.scss`
  (por exemplo, a cor da barra do topo é a variável `$cor-navbar`).
- **Ajuste fino no HTML** → use classes do Tailwind direto no `.jsp`
  (veja o "Bloco de exemplo (Tailwind)" no `index.jsp`).

> ⚠️ **Não edite** `src/main/webapp/css/styles.css` na mão — ele é **gerado**
> e será sobrescrito no próximo build. A fonte agora é o `styles.scss`.

## Fluxo completo ao mudar o visual

```bash
# 1) na pasta frontend, regenerar o CSS
cd frontend && npm run build

# 2) na raiz do projeto, reconstruir e subir o Tomcat
cd .. && ./mvnw package cargo:run
```

Depois, no navegador, dê **Ctrl + F5** (recarregar forçado) para ignorar o
cache do CSS antigo.

## (Opcional) Rodar o build do CSS junto com o Maven

Se quiser que o próprio `./mvnw package` gere o CSS, dá para usar o
`frontend-maven-plugin` (ele baixa o Node e roda `npm run build` no build do
Maven). Adicione ao `pom.xml`, dentro de `<build><plugins>`:

```xml
<plugin>
  <groupId>com.github.eirslett</groupId>
  <artifactId>frontend-maven-plugin</artifactId>
  <version>1.15.1</version>
  <configuration>
    <workingDirectory>frontend</workingDirectory>
    <nodeVersion>v20.11.1</nodeVersion>
  </configuration>
  <executions>
    <execution>
      <id>install-node-and-npm</id>
      <goals><goal>install-node-and-npm</goal></goals>
    </execution>
    <execution>
      <id>npm-install</id>
      <goals><goal>npm</goal></goals>
      <configuration><arguments>install</arguments></configuration>
    </execution>
    <execution>
      <id>npm-build-css</id>
      <goals><goal>npm</goal></goals>
      <phase>generate-resources</phase>
      <configuration><arguments>run build</arguments></configuration>
    </execution>
  </executions>
</plugin>
```

Enquanto não fizer isso, tudo bem: o `styles.css` já vem **gerado e
versionado**, então a aplicação funciona mesmo sem o Node instalado.
