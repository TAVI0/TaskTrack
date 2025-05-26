# Usa un JDK 17 oficial
FROM eclipse-temurin:17-jdk-jammy

# Directorio de trabajo
WORKDIR /app

# Copia Maven Wrapper y dependencias
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .

# Descarga dependencias sin ejecutar tests
RUN chmod +x mvnw && ./mvnw dependency:go-offline -B

# Copia el código fuente y compílalo
COPY src src
RUN ./mvnw clean package -DskipTests

# Expón el puerto que usa Spring Boot
EXPOSE 8080

# Arranca la aplicación
ENTRYPOINT ["java","-jar","target/tasktrack-backend-0.0.1-SNAPSHOT.jar"]
