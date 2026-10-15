# syntax=docker/dockerfile:1.7
FROM node:22-bookworm-slim AS web
WORKDIR /web
COPY apps/web/package.json apps/web/package-lock.json ./
RUN npm ci
COPY apps/web/ ./
RUN npm run build

FROM eclipse-temurin:25-jdk AS build
WORKDIR /src
COPY gradle gradle
COPY gradlew gradlew
COPY gradle.properties settings.gradle.kts build.gradle.kts ./
COPY gradle/libs.versions.toml gradle/libs.versions.toml
COPY build-logic build-logic
COPY apps apps
COPY mocks mocks
COPY --from=web /web/dist /src/apps/web/dist
RUN chmod +x gradlew && ./gradlew --no-daemon :apps:api:bootJar -PskipWeb

FROM eclipse-temurin:25-jre
RUN useradd --system --uid 10001 --create-home memorial \
    && apt-get update \
    && apt-get install -y --no-install-recommends curl \
    && rm -rf /var/lib/apt/lists/*
WORKDIR /app
COPY --from=build /src/apps/api/build/libs/*.jar /app/app.jar
USER memorial
EXPOSE 8080
ENV JAVA_OPTS="-XX:MaxRAMPercentage=75"
HEALTHCHECK --interval=20s --timeout=5s --start-period=50s --retries=8 \
  CMD curl -fsS http://127.0.0.1:8080/actuator/health/liveness || exit 1
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]
