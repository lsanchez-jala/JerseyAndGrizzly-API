# ── Build stage ──────
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:resolve dependency:resolve-plugins -q --fail-at-end || true
COPY src ./src
RUN mvn package -DskipTests

# ── Runtime stage ─────
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /app/target/order-mng-system-1.0-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]