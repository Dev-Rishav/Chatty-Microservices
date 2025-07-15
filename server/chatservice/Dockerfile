# Multi-stage Dockerfile for ChatService
# Production-ready configuration for AWS deployment

# Build stage
FROM maven:3.9.5-eclipse-temurin-21 AS builder

# Set working directory
WORKDIR /app

# Copy proto definitions first
COPY ../chattyprotos chattyprotos/

# Build proto definitions first
WORKDIR /app/chattyprotos
RUN mvn clean install -DskipTests

# Go back to root and copy service
WORKDIR /app
COPY chatservice/ chatservice/

# Build the service
WORKDIR /app/chatservice
RUN mvn clean package -DskipTests -Dspring.profiles.active=prod

# Runtime stage
FROM eclipse-temurin:21-jre-alpine

# Add labels for better organization
LABEL maintainer="dev.rishav@example.com"
LABEL version="1.0"
LABEL service="ChatService"
LABEL description="Chatty ChatService - Production Ready"

# Install curl for health checks
RUN apk add --no-cache curl

# Create non-root user
RUN addgroup -g 1001 -S spring && \
    adduser -S spring -u 1001

# Set working directory
WORKDIR /app

# Copy JAR from builder stage
COPY --from=builder /app/chatservice/target/*.jar app.jar

# Change ownership to spring user
RUN chown spring:spring app.jar

# Switch to non-root user
USER spring

# Expose port
EXPOSE 8081

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
  CMD curl -f http://localhost:8081/actuator/health || exit 1

# JVM optimizations for production
ENV JAVA_OPTS="-Xmx1024m -Xms512m -XX:+UseG1GC -XX:+UseStringDeduplication"

# Run the application
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar --spring.profiles.active=prod"]
