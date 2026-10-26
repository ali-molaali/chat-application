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

2. Create the Kafka topic (once per fresh Kafka container):

   ```bash
   docker exec chat-kafka /opt/kafka/bin/kafka-topics.sh --bootstrap-server localhost:19092 --create --if-not-exists --topic chat-messages --partitions 1 --replication-factor 1
   ```

   Kafka uses port 19092 because Windows reserved port 9092 on the development machine. Kafka data currently has no persistent Docker volume, so recreating the container can remove topics and records.

3. Start the application:

   ```bash
   mvn spring-boot:run
   ```

4. Check that it is up:

   ```bash
   curl http://localhost:8080/api/actuator/health
   ```

   The response should be `{"status":"UP", ...}`.

To stop the infrastructure, run `docker compose down`. Add `-v` to also delete the database data.

## Project structure

```
src/main/java/com/messagingapp/
  ChatApplicationMain.java     application entry point
  config/                      Spring security and password configuration
  controller/                  registration and message endpoints
  dto/                         validated API requests and responses
  entity/                      database entities
  event/                       Kafka event records
  exception/                   exceptions and API error handling
  repository/                  Spring Data repositories
  service/                     business logic and Kafka producer
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
- [x] User registration with validation, BCrypt, error handling and unit tests
- [x] Kafka topic created and console producer/consumer verified locally
- [x] Message event, producer service and REST endpoint implemented
- [ ] Verify `POST /api/messages` publishes JSON to Kafka
- [ ] Consume chat events in Java and save messages to PostgreSQL
- [ ] Real-time delivery to clients over WebSocket
- [ ] JWT authentication
- [ ] Integration tests with Testcontainers
- [ ] CI with GitHub Actions
- [ ] Dockerfile and deployment to AWS

## Current API checkpoint

- `POST /api/users/register`: register a user.
- `POST /api/messages`: accepts `senderId`, `roomId` and `text`; checks that the sender exists, generates an event ID and timestamp, and publishes to `chat-messages` using the room ID as the key. On successful Kafka acknowledgement, returns the event with `202 Accepted`.

The message endpoint is implemented but its live HTTP-to-Kafka verification is the next learning step. There is no Java consumer or message persistence yet. Both POST endpoints are currently public for local development; sender identity will come from authentication when JWT login is implemented.
