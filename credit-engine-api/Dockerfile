# Etapa de build
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

# Copia primeiro o pom.xml para aproveitar cache de dependencias
COPY pom.xml .
RUN mvn dependency:go-offline -B

COPY src ./src

# Roda os testes antes do empacotamento. Falha o build se algum teste quebrar.
RUN mvn clean test -B
RUN mvn clean package -DskipTests -B

# Etapa de runtime
FROM eclipse-temurin:21-jre-alpine AS runtime
WORKDIR /app

# Usuario nao root por seguranca
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]