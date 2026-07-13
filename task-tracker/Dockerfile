# syntax=docker/dockerfile:1

# ── Build stage ───────────────────────────────────────────────────────────────
# Compile and package the application in a full JDK image.
# The build output is copied into a leaner runtime image below.
FROM maven:3.9-eclipse-temurin-17 AS build

WORKDIR /app

# Copy POM first to cache the dependency download layer.
# Dependencies only re-download when pom.xml changes.
COPY pom.xml .
RUN mvn dependency:go-offline --batch-mode -q

# Copy source and package — skip tests since the test-runner handles testing
COPY src ./src
RUN mvn package -DskipTests --batch-mode -q

# ── Runtime stage ─────────────────────────────────────────────────────────────
# Use a JRE-only image for the final artifact — smaller than the full JDK image.
FROM eclipse-temurin:17-jre AS runtime

WORKDIR /app

# Copy only the packaged JAR from the build stage
COPY --from=build /app/target/*.jar app.jar

# Expose the port Spring Boot listens on
EXPOSE 8080

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]