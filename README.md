# TaskTrack Backend

Una API RESTful para gestionar tareas de forma colaborativa, desarrollada en Java Spring Boot y documentada con Swagger.

---

## 🚀 Descripción

Este backend provee:

* **Registro y autenticación** de usuarios (JWT).
* **CRUD** completo para **Tareas** (crear, leer, actualizar, eliminar, filtrar por estado/fecha).
* Documentación automática de todas las rutas mediante **Swagger UI**.
* Configuración para ejecución local con **Docker Compose** (PostgreSQL + aplicación).
* Pruebas unitarias e integración con **JUnit 5** y **Spring Boot Test**.

---

## 🛠️ Tecnologías

* **Java 21**
* **Spring Boot 3.4.5**
* **Spring Security (JWT)**
* **Spring Data JPA**
* **PostgreSQL 15**
* **Docker & Docker Compose**
* **Swagger/OpenAPI**
* **JUnit 5**, **Mockito**

---

## ⚙️ Pre-requisitos

* Git
* Java 21 (JDK)
* Maven 3.6+
* Docker & Docker Compose

---

## 🔧 Variables de entorno

| Variable                     | Descripción                          | Valor de ejemplo                            |
| ---------------------------- | ------------------------------------ | ------------------------------------------- |
| `SPRING_PROFILES_ACTIVE`     | Perfil Spring (`dev` o `prod`)       | `dev`                                       |
| `SPRING_DATASOURCE_URL`      | URL de conexión JDBC a PostgreSQL    | `jdbc:postgresql://db:5432/task`            |
| `SPRING_DATASOURCE_USERNAME` | Usuario de la base de datos          | `postgres`                                  |
| `SPRING_DATASOURCE_PASSWORD` | Password de la base de datos         | `postgres`                                  |
| `JWT_SECRET`                 | Clave secreta para firmar tokens JWT | `<tu_jwt_secret_de_al_menos_32_caracteres>` |
| `ALLOWED_ORIGIN`             | URL autorizada para filtro CORS      | `http://localhost:5173/`                    |

> Los valores de producción deben almacenarse de forma segura (e.g. Vault, AWS Secrets Manager).

---

## 🐳 Ejecución con Docker Compose

1. Clonar el repositorio y situarse en la raíz:

   ```bash
   git clone https://github.com/TAVI0/TaskTrack.git
   cd tasktrack
   ```
2. Crear archivo `.env` (opcional) si quieres sobreescribir variables:

   ```bash
   JWT_SECRET=MiSuperSecretoParaJWTDeAlMenos32Caracteres!
   ```
3. Levantar servicios:

   ```bash
   docker-compose up --build
   ```
4. La API quedará disponible en `http://localhost:8080`.

---

## 🏃 Ejecución local sin Docker

1. Asegúrate de tener PostgreSQL corriendo en `localhost:5432` con la base `task`, usuario `postgres` y password `postgres`.
2. Configurar variables de entorno:

   ```bash
   export SPRING_PROFILES_ACTIVE=dev
   export SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/task
   export SPRING_DATASOURCE_USERNAME=postgres
   export SPRING_DATASOURCE_PASSWORD=postgres
   export JWT_SECRET=MiSuperSecretoParaJWTDeAlMenos32Caracteres!
   ```
3. Compilar y ejecutar:

   ```bash
   mvn clean package
   java -jar target/tasktrack-backend-0.0.1-SNAPSHOT.jar
   ```

---

## 📖 Documentación Swagger

La documentación de todas las rutas y esquemas está disponible en:

> [https://tasktrack-go4j.onrender.com/swagger-ui/index.html](https://tasktrack-go4j.onrender.com/swagger-ui/index.html)

También puedes acceder localmente en:

```
http://localhost:8080/swagger-ui/index.html
```
