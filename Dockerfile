# syntax=docker/dockerfile:1

########## Etapa 1 – Build ##########
FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app

# 1️⃣ Copiamos solo el POM para cachear dependencias
COPY pom.xml .
RUN mvn -B dependency:go-offline

# 2️⃣ Copiamos el código y compilamos (sin tests)
COPY src ./src
RUN mvn -B clean package -DskipTests

########## Etapa 2 – Runtime ##########
FROM eclipse-temurin:21-jre
WORKDIR /app

# 3️⃣ Copiamos el fat-jar generado
COPY --from=build /app/target/*.jar app.jar

# 4️⃣ Exponemos y arrancamos
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
