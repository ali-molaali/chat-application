# Project Status

Last updated: 2026-10-14

This file tracks where the project is: what is finished, what we are working on now, and what comes next. It is updated after every step.

## Goal

Build a complete real-time chat application with Spring Boot, Apache Kafka and AWS, and publish it on GitHub as a portfolio project. The work is done in small steps, and each step is explained so it can be learned, not only copied.

## Where we are now

| | |
|---|---|
| Finished | Step 1 (local setup), steps 2a to 2c (`User` entity, `UserRepository`, `UserService`) |
| Working on | Step 2: user registration |
| Build state | The application starts (verified 2026-10-14). |
| Next small step | Step 2d: request and response DTOs with validation |
| Git | Repository: https://github.com/ali-molaali/chat-application, branch `main`. Every finished step is committed and pushed. |

## What we did

### Step 1: local setup

The project started as a bare Spring Boot skeleton that could not run, because there was no database and no Kafka for it to connect to.

- **`docker-compose.yml` created.** It starts two containers: PostgreSQL 16 (database `chat_db`, user `postgres`, password `postgres`, port 5432) and one Kafka broker (port 9092). Kafka runs in KRaft mode, which means it does not need ZooKeeper.
- **`flyway-database-postgresql` added to `pom.xml`.** Since Flyway 10, PostgreSQL support is a separate module. Without it the application fails at startup.
- **`App.java` and `AppTest.java` deleted.** They were the "Hello World" files that Maven generates. The test used JUnit 4, which Spring Boot 3 does not include, so it broke the build.
- **`.gitignore` created.** It keeps `target/`, IDE files and `.env` out of the repository.

Verified:

- The jar builds with JDK 21.
- The application starts in about 7 seconds and connects to PostgreSQL.
- `http://localhost:8080/api/actuator/health` returns 200.
- The Kafka broker is running and answers commands.

Not verified: the application does not talk to Kafka yet, because no code produces or consumes messages. That connection is first tested in step 3.

### Step 2a: `users` table and `User` entity

- **`src/main/resources/db/migration/V1__create_users_table.sql` created.** It creates the `users` table with the columns `id`, `username`, `email`, `password_hash` and `created_at`. `username` and `email` are unique.
- **`User.java` created.** It is the JPA entity mapped to the `users` table. It was first in a `user` package and moved to `entity` in step 2b.

Verified: Flyway applied migration version 1, Hibernate validation passed, and the application started. The table was checked directly in PostgreSQL.

### Step 2b: `UserRepository`

- **`src/main/java/com/messagingapp/repository/UserRepository.java` created.** It extends `JpaRepository<User, Long>` and adds `findByUsername`, `existsByUsername` and `existsByEmail`.
- **Package layout changed to layers.** The code is now organised as `entity`, `repository`, `service`, `service/impl`, `config`, `controller`, `dto` and so on, instead of one package per feature.

- **`User` moved to `entity/User.java`.** The old `user/User.java` was deleted, because two entities with the same name stop Hibernate from starting.

### Step 2c: `UserService`

- **`config/PasswordConfig.java` created.** A `@Configuration` class with a `@Bean` method that gives Spring one `BCryptPasswordEncoder`.
- **`service/UserService.java` created.** An interface with one method: `register(username, email, rawPassword)`.
- **`service/impl/UserServiceImpl.java` created.** It rejects a username or email that already exists, hashes the password with BCrypt, and saves the user. The repository and the password encoder are injected through the constructor.

Verified: the application starts, which proves Spring could create the repository and the service and inject their dependencies. Registration itself is tested once the endpoint exists (step 2e).

Problems met on the way, and their fixes:

- Edits in VS Code were not saved before committing, so empty files were pushed. Fix: turn on **File > Auto Save** and run `git status` before every commit.
- Auto-import picked `@Configurable` instead of `@Configuration`. Fix: check the package of an import before accepting it.
- `mvn spring-boot run` failed. The goal needs a colon: `mvn spring-boot:run`.
- "release version 21 not supported": Maven ran on JDK 17. Fix: `setx JAVA_HOME "C:\Program Files\Java\jdk-21.0.10"` and open a new terminal.
- "Connection to localhost:5432 refused": the containers were stopped. Fix: start Docker Desktop and run `docker compose up -d` before the app.

### Git and GitHub

- Local repository initialised on branch `main` and connected to GitHub.
- Every step is one commit, pushed to GitHub.

### Documentation

- **`README.md` created.** It is the public page for GitHub visitors: tech stack, how to run, structure and roadmap.
- **`PROJECT_STATUS.md` created.** This file.

## What we learned so far

- **Docker Compose** describes the local infrastructure in one file, so anyone can start the same PostgreSQL and Kafka with one command.
- **Flyway** versions the database schema. Each change is a new SQL file named `V<number>__<description>.sql` (two underscores). Flyway runs new files at startup and records them in the `flyway_schema_history` table. A migration that has already run is never edited; a new one is added instead.
- **A JPA entity** is a Java class where one object is one table row. `@Entity` marks the class, `@Table` names the table, `@Id` marks the primary key, and `@Column` maps a field to a column.
- **`ddl-auto: validate`** means Hibernate does not create or change tables. It only checks at startup that each entity matches its table. Flyway owns the schema and Hibernate verifies it.
- **Lombok** generates getters, setters and constructors at compile time from annotations such as `@Getter` and `@Setter`.
- **Passwords** are never stored. Only a hash is stored, which is why the column is called `password_hash`.
- **A Spring Data repository** is only an interface. Spring generates the implementation at startup and builds the SQL from method names such as `findByUsername`.
- **`Optional<User>`** is returned when a result may be missing. If no user is found it is an empty Optional, never `null`. It is opened with `orElseThrow`, `isPresent` or `ifPresent`.
- **`docker compose up -d`** starts the containers from `docker-compose.yml` in the background.
- **Git basics:** `git status` shows what will be committed, `git add` selects files, `git commit` records them, and `git push` uploads to GitHub. Running `git status` before every commit catches unsaved or missing files.
- **The three layers:** the controller receives HTTP requests, the service holds the business rules, and the repository talks to the database.
- **Beans and dependency injection:** Spring creates and manages objects (beans). A class declares what it needs as `private final` fields, Lombok's `@RequiredArgsConstructor` generates the constructor, and Spring passes the beans in. `@Service` and `@Configuration` with `@Bean` are two ways to create beans.
- **BCrypt** is a one-way, deliberately slow hash made for passwords.
- **`@Transactional`** runs a method as one database transaction: if it throws, nothing is saved.
- **Maven phases and goals:** a word like `package` is a phase; `plugin:goal` such as `spring-boot:run` runs a plugin goal.
- **Reading a stack trace:** the last `Caused by` line is the real cause.

## What we are doing now

Step 2 is the first real feature: a client can register a user. It is split into small parts.

| Part | What | Status |
|---|---|---|
| 2a | `users` table and `User` entity | Done |
| 2b | `UserRepository`: save and find users without writing SQL | Done |
| 2c | `UserService`: registration logic, password hashing with BCrypt, duplicate checks | Done |
| 2d | Request and response DTOs with validation | Next |
| 2e | `UserController`: `POST /api/users/register` | To do |
| 2f | Security configuration so the register endpoint is reachable without login | To do |
| 2g | Error handling: clear JSON errors for invalid input and duplicates | To do |
| 2h | Test the endpoint by hand, then with an automated test | To do |

## What we should do next

### Step 3: Kafka

- Create a `chat-messages` topic.
- Define the message event (sender, room, text, time).
- Write a producer: a REST endpoint receives a message and publishes it to the topic.
- Write a consumer: it reads from the topic and saves the message in PostgreSQL.
- Add a `messages` table with a `V2__` migration.
- Learn the Kafka basics on the way: topics, partitions, keys, consumer groups and offsets.

### Step 4: WebSocket

- Configure WebSocket with STOMP.
- The Kafka consumer pushes each message to the connected clients of that room.
- Add a very small HTML page to see messages arrive live.

### Step 5: security

- Login endpoint that returns a JWT.
- A filter that checks the JWT on every request.
- Secure the WebSocket connection.
- Move the JWT secret out of `application.yml` into an environment variable.

### Step 6: tests

- Unit tests for the services.
- Integration tests with Testcontainers, which start a real PostgreSQL and Kafka inside the test.

### Step 7: CI with GitHub Actions

- A workflow that builds and tests on every push.
- Git itself is already in use: each step is one commit, pushed to GitHub.

### Step 8: AWS

- Write a `Dockerfile` for the application.
- Push the image to Amazon ECR.
- Choose the services: for example ECS Fargate for the application, RDS for PostgreSQL and MSK for Kafka.
- Keep costs in mind. MSK is not in the free tier, so the choice is made when we reach this step.
- Store secrets in AWS Secrets Manager or Parameter Store.

## Known issues and open points

- **`JAVA_HOME` points to JDK 17.** The project needs Java 21, so a plain `mvn` command fails. JDK 21 is installed at `C:\Program Files\Java\jdk-21.0.10`. Until `JAVA_HOME` is changed in Windows, run this first in each terminal:

  ```powershell
  $env:JAVA_HOME = 'C:\Program Files\Java\jdk-21.0.10'
  ```

- **The JWT settings in `application.yml` are placeholders.** `spring.security.oauth2.jwt.secret` is not a real Spring property and contains a dummy secret. It is replaced in step 5.
- **All endpoints except health are locked.** Spring Security is on the classpath with no configuration, so it protects everything with a generated password. This is fixed in step 2f.
- **Kafka JSON trusted packages is `"*"`.** It is fine for local work and is narrowed to our own package in step 3.
- **The `prometheus` actuator endpoint is listed but not active.** It needs the `micrometer-registry-prometheus` dependency.
- **The project URL in `pom.xml` is a placeholder** (`yourusername`). It is set when the GitHub repository exists.
- **Logging is very verbose.** Spring Web and Kafka are on `DEBUG` in `application.yml`.

## Useful commands

```powershell
# Start and stop PostgreSQL and Kafka
docker compose up -d
docker compose down

# Run the application
mvn spring-boot:run

# Health check
curl http://localhost:8080/api/actuator/health

# Look at a table
docker exec -it chat-postgres psql -U postgres -d chat_db -c "\d users"

# List Kafka topics
docker exec chat-kafka /opt/kafka/bin/kafka-topics.sh --bootstrap-server localhost:9092 --list
```
