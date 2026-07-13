# Task Tracker — Containerized Integration Test Suite

A Spring Boot REST API backed by PostgreSQL, containerized with Docker Compose and validated by a REST Assured integration test suite. Three containers — database, API, and test runner — are orchestrated with healthcheck-gated dependencies. GitHub Actions CI builds both images, spins up the full stack, runs 30 integration tests, and tears everything down on every push.

## Architecture

```
task-tracker-portfolio/
├── task-tracker/          Spring Boot REST API
│   ├── src/
│   └── Dockerfile         Multi-stage build — JDK for compile, JRE for runtime
├── test-runner/           REST Assured integration test suite
│   ├── src/
│   └── Dockerfile         Single-stage Maven build and test execution
├── docker-compose.yml     Local three-container orchestration
├── docker-compose.ci.yml  CI orchestration using pre-built images
└── .github/workflows/
    └── integration-tests.yml
```

## API Design

### Users

| Method | Endpoint       | Description          | Status |
|--------|---------------|----------------------|--------|
| POST   | /users         | Create a user        | 201    |
| GET    | /users/{id}    | Get a user by ID     | 200    |
| DELETE | /users/{id}    | Delete a user        | 204    |

### Tasks

| Method | Endpoint              | Description               | Status |
|--------|-----------------------|---------------------------|--------|
| POST   | /tasks                | Create a task             | 201    |
| GET    | /tasks/{id}           | Get a task by ID          | 200    |
| GET    | /tasks                | Get all tasks with filters | 200   |
| PUT    | /tasks/{id}           | Update a task             | 200    |
| PATCH  | /tasks/{id}/status    | Transition task status    | 200    |
| DELETE | /tasks/{id}           | Delete a task             | 204    |

### Task Status State Machine

Status transitions are enforced at the service layer and return 409 Conflict on invalid attempts.

```
OPEN ──────────► IN_PROGRESS ──────────► DONE
                      │
                      ▼
                    OPEN
```

Valid transitions:
- `OPEN` → `IN_PROGRESS`
- `IN_PROGRESS` → `DONE`
- `IN_PROGRESS` → `OPEN` (reopen)

Invalid transitions (409 Conflict):
- `OPEN` → `DONE`
- `DONE` → anything (completed tasks are locked)

### Query Parameters — GET /tasks

```
GET /tasks?status=OPEN
GET /tasks?priority=HIGH
GET /tasks?userId={id}
GET /tasks?status=IN_PROGRESS&priority=HIGH
GET /tasks?sortBy=priority&order=asc
GET /tasks?sortBy=priority&order=desc
```

---

## Running Locally

### Prerequisites

- Docker Desktop

No local Java or Maven installation required.

### Start the full stack

```bash
docker-compose up --build
```

Compose starts postgres, waits for it to be healthy, starts the API and runs Flyway migrations, waits for the health endpoint to return healthy, then starts the test runner. Output tails all three containers. The test runner exits when the suite completes.

### Tear down

```bash
docker-compose down --volumes
```

The `--volumes` flag removes the database volume so the next run starts with a clean schema.

### Run a single test class

```bash
docker-compose run --rm test-runner mvn test -Dtest=UserTests --batch-mode
```

---

## Container Architecture

Three containers with a linear dependency chain:

```
postgres
  └─► task-tracker  (waits for pg_isready)
        └─► test-runner  (waits for /actuator/health)
```

**Healthcheck-gated dependencies** — `depends_on` with `condition: service_healthy` ensures each container only starts when the previous one is genuinely ready, not just started. Without this, the API would attempt to connect to a database that hasn't finished initializing, and the tests would run before the API is accepting requests.

**Two Compose files** — `docker-compose.yml` builds images from source for local development. `docker-compose.ci.yml` references pre-built tagged images (`task-tracker:ci`, `test-runner:ci`) produced by the GitHub Actions build steps, avoiding redundant builds inside the Compose run.

**Multi-stage API Dockerfile** — the build stage uses the full Maven + JDK image to compile and package the JAR. The runtime stage copies only the JAR into a JRE-only image, producing a significantly smaller final artifact with no compiler or source code included.

---

## Test Coverage

30 tests across three test classes.

### UserTests (9 tests)

- Create user with valid payload — 201 with generated ID
- Create user with duplicate email — 409
- Create user missing name — 400
- Create user missing email — 400
- Get user by valid ID — 200 with correct data
- Get user by invalid ID — 404
- Delete user by valid ID — 204
- Delete user by invalid ID — 404
- Delete user cascades task deletion — verify tasks return 404 after user deleted

### TaskCrudTests (13 tests)

- Create task with valid payload — 201 with all fields
- Create task — default status is OPEN
- Create task — default priority is MEDIUM
- Create task with invalid user ID — 404
- Create task missing title — 400
- Create task missing user ID — 400
- Get task by valid ID — 200 with correct data
- Get task by invalid ID — 404
- Update task with valid payload — 200 with updated fields
- Update task — updatedAt timestamp changes
- Update task with invalid ID — 404
- Delete task by valid ID — 204
- Delete task by invalid ID — 404

### TaskStatusTests (8 tests)

- OPEN → IN_PROGRESS — 200
- IN_PROGRESS → DONE — 200
- IN_PROGRESS → OPEN (reopen) — 200
- OPEN → DONE (invalid) — 409
- DONE → OPEN (invalid) — 409
- DONE → IN_PROGRESS (invalid) — 409
- Invalid task ID — 404
- Invalid status value — 400

---

## Database Schema

```sql
CREATE TABLE users (
    id         BIGSERIAL    PRIMARY KEY,
    name       VARCHAR(100) NOT NULL,
    email      VARCHAR(255) NOT NULL UNIQUE,
    created_at TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE TABLE tasks (
    id          BIGSERIAL    PRIMARY KEY,
    title       VARCHAR(255) NOT NULL,
    description TEXT,
    status      VARCHAR(20)  NOT NULL DEFAULT 'OPEN',
    priority    VARCHAR(10)  NOT NULL DEFAULT 'MEDIUM',
    user_id     BIGINT       NOT NULL,
    created_at  TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMP    NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT chk_status CHECK (status IN ('OPEN', 'IN_PROGRESS', 'DONE')),
    CONSTRAINT chk_priority CHECK (priority IN ('LOW', 'MEDIUM', 'HIGH'))
);
```

Schema is version-controlled via Flyway migrations in `task-tracker/src/main/resources/db/migration/`. Flyway runs automatically on API startup.

---

## CI/CD

The GitHub Actions workflow runs on every push and pull request to `main`.

1. Build `task-tracker` image with GHA layer caching scoped to the API
2. Build `test-runner` image with GHA layer caching scoped to the tests
3. Run `docker compose -f docker-compose.ci.yml up --exit-code-from test-runner`
4. Tear down all containers and volumes — always runs regardless of test outcome
5. Upload TestNG reports as artifacts with 14-day retention

Layer caching is scoped separately per image so a change to test code doesn't invalidate the API image cache, and vice versa.

---

## Technologies

- **Java 17** — API and test suite
- **Spring Boot 3.3.5** — REST API framework
- **Spring Data JPA / Hibernate** — database access
- **PostgreSQL 16** — relational database
- **Flyway** — schema migration
- **Spring Boot Actuator** — health endpoint for container healthcheck
- **REST Assured 5.4.0** — integration test DSL
- **TestNG 7.9.0** — test execution and assertions
- **Jackson 2.17.0** — JSON serialization
- **Docker** — containerization
- **Docker Compose** — multi-container orchestration
- **Maven** — build tool
- **GitHub Actions** — CI/CD pipeline
