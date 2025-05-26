# Etapa 1: Build con Maven
FROM maven:3.9.0-openjdk-17-slim AS build
WORKDIR /app

# Solo copiamos pom.xml primero para cachear dependencias
COPY pom.xml .
# Si usas Maven Wrapper en lugar del cli, reemplaza la línea anterior por:
# COPY mvnw pom.xml .
# COPY .mvn .mvn

RUN mvn dependency:go-offline -B

# Ahora copiamos el resto del código y construimos el jar
COPY src ./src
RUN mvn clean package -DskipTests -B

# Etapa 2: Runtime con JRE liviano
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app

# Copiamos el fat-jar generado
COPY --from=build /app/target/*.jar app.jar

# Puerto expuesto por tu aplicación Spring Boot
EXPOSE 8080

# Comando de arranque
ENTRYPOINT ["java", "-jar", "app.jar"]
