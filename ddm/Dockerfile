# syntax=docker/dockerfile:1

# ---- build stage ----
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /build

# Cache dependencies separately from source for faster rebuilds.
COPY pom.xml .
COPY .mvn .mvn
COPY mvnw .
RUN mvn -B -q dependency:go-offline || true

COPY src src
RUN mvn -B -DskipTests clean package

# ---- runtime stage ----
FROM eclipse-temurin:17-jre
WORKDIR /app

# Spring Boot fat jar produced above (ddm-backend-<version>.jar).
COPY --from=build /build/target/*.jar app.jar

# Matches server.port=${API_PORT:8088}.
EXPOSE 8088

ENTRYPOINT ["java", "-jar", "/app/app.jar"]