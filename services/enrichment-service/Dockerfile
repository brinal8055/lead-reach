FROM gradle:8.5-jdk17 AS builder
WORKDIR /app
COPY services/enrichment-service/ .
COPY proto/ src/main/proto/
ENV DOCKER_BUILD=true
RUN gradle build --no-daemon

FROM openjdk:17-slim
WORKDIR /app
COPY --from=builder /app/build/libs/*-all.jar app.jar
EXPOSE 50052
ENTRYPOINT ["java", "-jar", "app.jar"]
