# Project Status

Last updated: 2026-10-26

This file tracks where the project is: what is finished, what we are working on now, and what comes next. It is updated after every step.

## Goal

Build a complete real-time chat application with Spring Boot, Apache Kafka and AWS, and publish it on GitHub as a portfolio project. The work is done in small steps, and each step is explained so it can be learned, not only copied.

## Where we are now

| | |
|---|---|
| Finished | Steps 1 and 2a to 2h; Kafka console exercise; event, DTO, producer, message service and controller implemented |
| Working on | Step 3: Kafka. The REST producer is written; live HTTP-to-Kafka verification is next. |
| Build state | `mvn test` passed on JDK 21 on 2026-10-26: all 18 main source files compiled and all 3 existing registration tests passed. Latest full application startup has not been reverified. |
| Next small step | Start the app, send `POST /api/messages` with an existing user ID, and observe the JSON in the console consumer |
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

### Step 2d: request and response DTOs

- **`dto/RegisterRequest.java` created.** A record with `username`, `email` and `password`, and validation rules: username 3 to 50 characters (letters, digits and `_` only), a valid email, and a password of 8 to 72 characters.
- **`dto/UserResponse.java` created.** A record with `id`, `username`, `email` and `createdAt`, and a `from(User)` method that converts an entity into a response. It has no password field, so the hash can never be sent to a client.

Verified: both files compile. The validation rules take effect in step 2e, when the controller marks the request with `@Valid`.

Problem met on the way: the files were committed twice as empty skeletons before the code was saved. The check that catches this is `git diff --staged`, which shows the content that will be committed.

### Step 2e: `UserController`

- **`controller/UserController.java` created.** `POST /api/users/register` takes a `RegisterRequest` as JSON (`@RequestBody`), validates it (`@Valid`), calls `UserService.register`, and returns a `UserResponse` with status `201 Created`.

Tested in step 2f, once the endpoint was opened.

Problems met on the way:

- VS Code showed "variable userService not initialized in the default constructor" and "Flexible Constructor Bodies is only available with source level 25". This is a false error from the editor's built-in Lombok, not a real problem: `mvn compile` succeeds. Writing the constructor by hand removes the red line, if it gets in the way.
- Java autocomplete and auto-import did not work. Fix: install Extension Pack for Java, set `java.configuration.runtimes` to JDK 21, and run **Java: Clean Java Language Server Workspace**. Do not set `java.jdt.ls.java.home` to JDK 21, because the extension itself needs Java 25 and brings its own.
- The "Kilo Code Autocomplete has been paused" message is from a separate AI extension and is not related to Java autocomplete.

### Step 2f: security configuration

- **`config/SecurityConfig.java` created.** A `SecurityFilterChain` bean: CSRF off, stateless sessions, `POST /users/register`, `/actuator/health` and `/error` open to everyone, and every other endpoint requires authentication.

Verified: the first real user was registered with `Invoke-RestMethod` and checked in PostgreSQL. The password is stored as a BCrypt hash. At that point registering the same user twice returned `500`; step 2g fixed it.

### Step 2g: error handling

- **`exception/UserAlreadyExistsException.java` created.** Our own exception for a username or email that is already registered.
- **`exception/GlobalExceptionHandler.java` created.** A `@RestControllerAdvice` that turns `UserAlreadyExistsException` into `409 Conflict` and validation failures into `400 Bad Request` with an `errors` map per field. Both use `ProblemDetail`, the standard JSON error format (RFC 9457).
- **`UserServiceImpl` changed** to throw `UserAlreadyExistsException` instead of `IllegalArgumentException`.

Verified: a duplicate registration returns `409` instead of `500`.

### Step 2h: first unit tests

- **`src/test/java/com/messagingapp/service/impl/UserServiceImplTest.java` created.** Three Mockito unit tests for `UserServiceImpl`: a new user is saved with a hashed password, a taken username is rejected, and a taken email is rejected. The two rejection tests also check that nothing is saved.

Verified: `mvn test` runs without Docker or a running app.

### Step 3: Kafka producer checkpoint

- **Infrastructure:** Windows excludes ports 9081-9180, including 9092. Kafka now listens and advertises `localhost:19092`; both Spring bootstrap-server settings match it. Explicit KRaft node, controller, listener protocol and single-broker replication settings fixed the startup failure.
- **Topic:** `chat-messages` created with one partition and replication factor 1. The learner verified console publishing, consuming and rereading retained records.
- **`event/ChatMessageEvent.java`:** record containing `eventId`, `senderId`, `roomId`, `text` and `sentAt`.
- **`dto/SendMessageRequest.java`:** validates a positive sender ID and nonblank, length-limited room and message text.
- **`service/ChatMessageProducer.java`:** sends JSON events with `KafkaTemplate`, using the room ID as the record key; returns the send future.
- **`service/ChatMessageService.java`:** checks the sender exists, generates a UUID and timestamp, and returns the event after the Kafka send succeeds.
- **`exception/UserNotFoundException.java`:** mapped to a `404` ProblemDetail by the existing global handler.
- **`controller/ChatMessageController.java`:** `POST /api/messages`, validation and asynchronous `202 Accepted` response after Kafka acknowledgement.
- **Security:** message POST temporarily public for local testing; async dispatch permitted so Spring can complete the future-based response.

Verification: console Kafka flow confirmed by the learner. On 2026-10-26, `mvn test` passed on JDK 21: 18 main source files compiled; 3 existing registration tests passed with no failures or errors. These tests do not cover message publishing. Live REST publishing, error responses and application startup with the new beans are still to be tested. There is no Java consumer or messages table yet.

Learning mode: the assistant explains and reviews; the learner writes the application code and runs the lesson exercises. Documentation maintenance and this Git checkpoint were explicitly delegated to the assistant.

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
- **DTOs** describe exactly what an endpoint accepts and returns. They keep internal fields such as `passwordHash` out of the API and stop clients from setting fields such as `id`.
- **Java records** are short classes that only hold data. Java generates the constructor, accessors, `equals` and `toString`.
- **Bean Validation** annotations such as `@NotBlank`, `@Size`, `@Email` and `@Pattern` describe rules for input. Spring checks them when the parameter is marked `@Valid`.
- **A REST controller** receives HTTP requests. `@RestController` turns return values into JSON, `@RequestMapping` and `@PostMapping` map URLs, `@RequestBody` reads the JSON body, and `@ResponseStatus` sets the status code.
- **Constructor injection:** Spring passes a class's dependencies into its constructor. `@RequiredArgsConstructor` writes that constructor; it can also be written by hand.
- **Spring Security filter chain:** every request passes security filters before reaching a controller. `permitAll()` opens a path and `authenticated()` requires login. Rules are checked top to bottom, so `anyRequest()` comes last. Paths are written without the `/api` context path.
- **CSRF** protects cookie-based logins. A token-based REST API turns it off.
- **`/error` must be open,** because Spring forwards there to build error responses.
- **`Invoke-RestMethod`** is PowerShell's command for calling a REST API.
- **Unit tests with Mockito:** `@Mock` creates fake dependencies, `@InjectMocks` builds the class under test with them, `when(...).thenReturn(...)` sets their answers, and `verify(...)` checks how they were called. Each test follows arrange, act, assert.
- **Test code** lives in `src/test/java`, in the same package as the class it tests, and is never part of the application. `mvn test` runs it.
- **HTTP status codes used so far:** `201 Created` for a new resource, `400 Bad Request` for invalid input, `401`/`403` for blocked access, `409 Conflict` for a duplicate, and `500` for a server bug.
- **`@RestControllerAdvice` with `@ExceptionHandler`** handles exceptions from all controllers in one place.
- **`ProblemDetail`** is Spring's class for the standard error format with `type`, `title`, `status` and `detail`.
- **`curl.exe -i` with `--data-binary "@body.json"`** shows the full response including an error body, and avoids JSON quoting problems in PowerShell.
- **The git pager:** long output opens in a viewer; `q` quits it.
- **VS Code shortcuts:** `Ctrl+Space` shows suggestions, `Ctrl+.` fixes the problem under the cursor (for example a missing import), `Shift+Alt+O` organises imports, and `Shift+Alt+F` formats the file.
- **Staged vs not staged:** only staged changes go into a commit. `git status --short` shows staged changes with the letter on the left. `git diff --staged` shows their content.

## What we are doing now

Step 2 (registration) is complete. Its parts are recorded below; current work is the step 3 Kafka producer checkpoint described above.

| Part | What | Status |
|---|---|---|
| 2a | `users` table and `User` entity | Done |
| 2b | `UserRepository`: save and find users without writing SQL | Done |
| 2c | `UserService`: registration logic, password hashing with BCrypt, duplicate checks | Done |
| 2d | Request and response DTOs with validation | Done |
| 2e | `UserController`: `POST /api/users/register` | Done |
| 2f | Security configuration so the register endpoint is reachable without login | Done |
| 2g | Error handling: clear JSON errors for invalid input and duplicates | Done |
| 2h | Test the endpoint by hand, then with an automated test | Done |

## What we should do next

### Step 3: Kafka

- Completed: create a `chat-messages` topic and verify console publishing/consuming.
- Implemented: message event, validated request, producer service and REST endpoint.
- Next: verify HTTP publishing and the `202`, `400` and `404` responses manually.
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
- **Message sender identity is not authenticated yet.** Registration and message POST endpoints are public for local development. Checking that `senderId` exists does not prove the caller owns that account; JWT authentication is planned in step 5.
- **Kafka data is not persisted in a Docker volume.** Recreating its container can remove topics and records. Recreate `chat-messages` using the README command if necessary.
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
docker exec chat-kafka /opt/kafka/bin/kafka-topics.sh --bootstrap-server localhost:19092 --list
```
