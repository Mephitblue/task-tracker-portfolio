# syntax=docker/dockerfile:1

# Single-stage build — Maven compiles and runs the tests in one step.
# No runtime stage needed since the container exits after the test run completes.
FROM maven:3.9-eclipse-temurin-17 AS test-runner

WORKDIR /app

# Copy POM first to cache the dependency download layer.
# Dependencies only re-download when pom.xml changes.
COPY pom.xml .
RUN mvn dependency:go-offline --batch-mode -q

# Copy test source after the dependency layer is cached
COPY src ./src

# BASE_URL is passed at runtime via environment variable.
# Defaults to localhost for local execution without Docker.
ENV BASE_URL=http://localhost:8080

# Default command: run the full test suite.
CMD ["mvn", "test", "--batch-mode"]