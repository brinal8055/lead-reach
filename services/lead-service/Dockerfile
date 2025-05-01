FROM gradle:8.5-jdk17 AS builder
WORKDIR /app
COPY services/lead-service/ .
COPY proto/ src/main/proto/
ENV DOCKER_BUILD=true
RUN gradle build --no-daemon

FROM openjdk:17-slim
WORKDIR /app
COPY --from=builder /app/build/libs/*-all.jar app.jar

# Install netcat for health checks
RUN apt-get update && apt-get install -y netcat-openbsd && rm -rf /var/lib/apt/lists/*

EXPOSE 50055
ENTRYPOINT ["java", "-jar", "app.jar"]
