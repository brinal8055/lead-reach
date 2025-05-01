FROM gradle:8.5-jdk17 AS builder
WORKDIR /app
COPY services/orchestrator/ .
COPY proto/ src/main/proto/
ENV DOCKER_BUILD=true
RUN gradle build --no-daemon

FROM openjdk:17-slim
WORKDIR /app
COPY --from=builder /app/build/libs/*-all.jar app.jar
EXPOSE 50051 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
