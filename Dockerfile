# ============================================================
#  Dockerfile - build do .war (Maven) + runtime no Tomcat 10.1
#  Multi-stage: a imagem final NAO carrega o Maven, so o Tomcat.
# ============================================================

# ---------- Etapa 1: build do .war com Maven ----------
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

# 1) copia so o pom e baixa as dependencias (camada em cache;
#    so refaz o download se o pom.xml mudar)
COPY pom.xml .
RUN mvn -q -DskipTests dependency:go-offline

# 2) copia o codigo e empacota o .war
#    (o styles.css do Sass/Tailwind ja vem versionado em src/, entao
#     nao precisa de Node aqui. O cargo NAO roda: nada de baixar Tomcat.)
COPY src ./src
RUN mvn -q -DskipTests clean package

# ---------- Etapa 2: runtime no Tomcat ----------
FROM tomcat:10.1-jdk21-temurin
# remove os apps de exemplo do Tomcat
RUN rm -rf /usr/local/tomcat/webapps/*
# publica como "NewProject004" para manter a URL /NewProject004/
COPY --from=build /app/target/NewProject004-1.0-SNAPSHOT.war \
     /usr/local/tomcat/webapps/NewProject004.war

EXPOSE 8080
CMD ["catalina.sh", "run"]

