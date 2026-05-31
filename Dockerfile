# Stage 1: Build — compiles and packages the application
FROM maven:3.9-eclipse-temurin-22 AS builder

WORKDIR /app

# Copy pom.xml first to cache dependency resolution as a separate layer.
# Dependencies are re-downloaded only when pom.xml changes.
COPY pom.xml .
RUN mvn -q dependency:resolve-plugins dependency:resolve

# Copy source and build the JAR (tests skipped — run separately in CI)
COPY src ./src
RUN mvn -q clean package -DskipTests

# Stage 2: Runtime — minimal JRE image for production
FROM eclipse-temurin:22-jre-jammy

LABEL maintainer="Emerson Lima"
LABEL version="1.0"
LABEL description="MiniAuthorizer — Spring Boot 3.3 + MongoDB"

WORKDIR /app

# Non-root user for security hardening
RUN groupadd -r appuser && useradd -r -g appuser appuser

# Copy the fat JAR from the builder stage
COPY --from=builder /app/target/miniauthorizer-*.jar application.jar

RUN chown appuser:appuser /app/application.jar

USER appuser

# Health check via Spring Boot Actuator liveness endpoint.
# Requires spring-boot-starter-actuator on the classpath.
HEALTHCHECK --interval=30s --timeout=5s --start-period=40s --retries=3 \
    CMD curl -f http://localhost:8080/actuator/health/liveness || exit 1

EXPOSE 8080

# JVM tuning for containerized environments
ENV JVM_OPTS="-Xms256m -Xmx512m -XX:+UseG1GC -XX:MaxGCPauseMillis=200 -Dfile.encoding=UTF-8"

ENTRYPOINT ["sh", "-c", "java $JVM_OPTS -jar application.jar"]
