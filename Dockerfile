# Etapa 1: Construcción del jar usando Maven
FROM eclipse-temurin:17-jdk-jammy
WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests

# Etapa 2: Ejecutar el jar en una imagen más liviana
FROM eclipse-temurin:17-jdk-jammy
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
