# Chat Application

A real-time messaging backend built with Spring Boot, Apache Kafka and PostgreSQL, to be deployed on AWS.

This project is under active development. The [roadmap](#roadmap) shows what works today and what is planned.

## Tech stack

| Area | Technology |
|---|---|
| Language | Java 21 |
| Framework | Spring Boot 3.3 (Web, Data JPA, Security, WebSocket, Actuator) |
| Messaging | Apache Kafka (Spring Kafka) |
| Database | PostgreSQL 16, schema managed by Flyway |
| Build | Maven |
| Local infrastructure | Docker Compose |
| Cloud | AWS (planned) |

## Running locally

You need JDK 21, Maven and Docker.

1. Start PostgreSQL and Kafka:

   ```bash
   docker compose up -d
   ```

2. Start the application:

   ```bash
   mvn spring-boot:run
   ```

3. Check that it is up:

   ```bash
   curl http://localhost:8080/api/actuator/health
   ```

   The response should be `{"status":"UP", ...}`.

To stop the infrastructure, run `docker compose down`. Add `-v` to also delete the database data.

## Project structure

```
src/main/java/com/messagingapp/
  ChatApplicationMain.java     application entry point
  user/                        user accounts
src/main/resources/
  application.yml              configuration
  db/migration/                Flyway SQL migrations (V1__, V2__, ...)
docker-compose.yml             local PostgreSQL and Kafka
```

## Database migrations

Flyway owns the database schema. Every schema change is a new SQL file in `src/main/resources/db/migration`, and Flyway applies new files at startup. Hibernate runs with `ddl-auto: validate`, so it only checks that the entities match the tables and never changes them.

## Roadmap

- [x] Local setup: Spring Boot skeleton, Docker Compose with PostgreSQL and Kafka
- [x] `users` table (Flyway migration) and `User` entity
- [ ] User repository and registration endpoint
- [ ] Send and consume chat messages through a Kafka topic
- [ ] Real-time delivery to clients over WebSocket
- [ ] JWT authentication
- [ ] Integration tests with Testcontainers
- [ ] CI with GitHub Actions
- [ ] Dockerfile and deployment to AWS
