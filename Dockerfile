# Estágio 1: Build
FROM maven:3.8.6-eclipse-temurin-17 AS build
WORKDIR /app

# Copia apenas o pom.xml para baixar as dependências (otimiza o cache do Docker)
COPY pom.xml .
RUN mvn dependency:go-offline

# Copia o código fonte e gera o pacote
COPY src ./src
RUN mvn clean package -DskipTests

# Estágio 2: Runtime
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Copia o jar gerado no estágio anterior
COPY --from=build /app/target/*.jar trabalho-2-1.0-SNAPSHOT.jar.jar

# Define variáveis de ambiente padrão (podem ser subscritas pelo Compose)
ENV DB_URL=jdbc:postgresql://db:5432/users
ENV DB_USER=postgres
ENV DB_PASSWORD=password

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "trabalho-2-1.0-SNAPSHOT.jar"]