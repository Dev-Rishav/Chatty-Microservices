# Multi-stage Dockerfile for Spring Boot services

# Build stage
FROM maven:3.9.5-eclipse-temurin-21 AS builder

# Set working directory
WORKDIR /app

# Copy proto definitions first
COPY chattyprotos chattyprotos/

# Build proto definitions first
WORKDIR /app/chattyprotos
RUN mvn clean install -DskipTests

# Go back to root
WORKDIR /app

# Copy individual service
ARG SERVICE_NAME
COPY ${SERVICE_NAME} ${SERVICE_NAME}/

# Build the service
WORKDIR /app/${SERVICE_NAME}
RUN mvn clean package -DskipTests

# Runtime stage
FROM eclipse-temurin:21-jre-alpine

# Add labels for better organization
LABEL maintainer="dev.rishav@example.com"
LABEL version="1.0"
LABEL description="Chatty Microservices - Production Ready"

# Create non-root user
RUN addgroup -g 1001 -S spring && \
    adduser -S spring -u 1001

# Set working directory
WORKDIR /app

# Copy JAR from builder stage
ARG SERVICE_NAME
COPY --from=builder /app/${SERVICE_NAME}/target/*.jar app.jar

# Change ownership to spring user
RUN chown spring:spring app.jar

# Switch to non-root user
USER spring

# Expose port (will be overridden by Railway)
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
  CMD curl -f http://localhost:8080/actuator/health || exit 1

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
CMD ["--spring.profiles.active=prod"]
