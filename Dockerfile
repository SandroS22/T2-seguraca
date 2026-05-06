# Estágio 1: Build (JDK 21)
FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app

# Copia o pom.xml para cache de dependências
COPY pom.xml .
RUN mvn dependency:go-offline

# Copia o código e compila
COPY src ./src
RUN mvn clean package -DskipTests

# Estágio 2: Runtime (JRE 21)
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

COPY --from=build /app/target/*.jar app.jar

ENV DB_URL=jdbc:postgresql://db:5432/meu_banco
ENV DB_USER=postgres
ENV DB_PASSWORD=password

EXPOSE 8080

ENTRYPOINT ["java", "-cp", "app.jar", "org.ufsc.Main"]