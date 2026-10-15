# syntax=docker/dockerfile:1.7
# Expects :apps:api:bootJar (and the Vite build) to have already run.
FROM eclipse-temurin:25-jre
RUN useradd --system --uid 10001 --create-home memorial \
    && apt-get update \
    && apt-get install -y --no-install-recommends curl \
    && rm -rf /var/lib/apt/lists/*
WORKDIR /app
COPY apps/api/build/libs/*.jar /app/app.jar
USER memorial
EXPOSE 8080
ENV JAVA_OPTS="-XX:MaxRAMPercentage=75"
HEALTHCHECK --interval=20s --timeout=5s --start-period=50s --retries=8 \
  CMD curl -fsS http://127.0.0.1:8080/actuator/health/liveness || exit 1
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]
