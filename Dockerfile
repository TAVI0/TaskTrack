# Etapa 1: build con Maven y Java 21
FROM eclipse-temurin:21-jdk-jammy as build
WORKDIR /app

# Instalamos Maven manualmente
RUN apt-get update && \
    apt-get install -y maven && \
    apt-get clean

COPY pom.xml .
RUN mvn dependency:go-offline -B

COPY src ./src
RUN mvn clean package -DskipTests -B

# Etapa 2: runtime con JDK 21 liviano
FROM eclipse-temurin:21-jdk-jammy
WORKDIR /app

COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
