# syntax=docker/dockerfile:1.7
FROM node:24-bookworm-slim@sha256:a5f4366e834695d5f363b920c104953a535d3a9864d1bdf74566a0ebe01daee5 AS web
WORKDIR /web
COPY apps/web/package.json apps/web/package-lock.json ./
RUN npm ci
COPY apps/web/ ./
RUN npm run build

FROM eclipse-temurin:25-jdk@sha256:63f965b560b905c9633218330ac3516304b526a74fdc323a504b05c06e712c30 AS build
WORKDIR /src
COPY gradle gradle
COPY gradlew gradlew
COPY gradle.properties settings.gradle.kts build.gradle.kts ./
COPY gradle/libs.versions.toml gradle/libs.versions.toml
COPY build-logic build-logic
COPY apps apps
COPY mocks mocks
COPY --from=web /web/dist /src/apps/web/dist
RUN chmod +x gradlew && ./gradlew --no-daemon :apps:api:bootJar -PskipWeb=true

FROM eclipse-temurin:25-jre@sha256:fd4b1c02c64eef378212e23656fc0b0caa41a357876fa0b60e51ceda94a3db64
RUN useradd --system --uid 10001 --create-home memorial
WORKDIR /app
COPY --from=build /src/apps/api/build/libs/api-0.1.0.jar /app/app.jar
USER memorial
EXPOSE 8080
HEALTHCHECK --interval=20s --timeout=5s --start-period=50s --retries=8 \
  CMD ["/bin/bash", "-c", "exec 3<>/dev/tcp/127.0.0.1/8080"]
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75", "-jar", "/app/app.jar"]
