# Etapa 1: build con Maven + JDK 21
FROM maven:3.9.4-jdk-21-slim AS build
WORKDIR /app

# 1) Cacheamos dependencias copiando solo el pom
COPY pom.xml .
RUN mvn dependency:go-offline -B

# 2) Copiamos el código y empaquetamos sin tests
COPY src ./src
RUN mvn clean package -DskipTests -B

# Etapa 2: runtime con JDK 21 liviano
FROM eclipse-temurin:21-jdk-jammy
WORKDIR /app

# 3) Traemos el jar compilado
COPY --from=build /app/target/*.jar app.jar

# 4) Exponemos el puerto de Spring Boot y arrancamos
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
