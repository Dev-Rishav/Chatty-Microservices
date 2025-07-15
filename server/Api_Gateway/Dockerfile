# Multi-stage Dockerfile for API Gateway
# Production-ready configuration for AWS deployment

# Build stage
FROM maven:3.9.5-eclipse-temurin-21 AS builder

# Set working directory
WORKDIR /app

# Copy the current directory contents (Api_Gateway)
COPY . .

# Build the service (already in Api_Gateway directory)
RUN mvn clean package -DskipTests -Dspring.profiles.active=prod

# Runtime stage
FROM eclipse-temurin:21-jre-alpine

# Add labels for better organization
LABEL maintainer="dev.rishav@example.com"
LABEL version="1.0"
LABEL service="ApiGateway"
LABEL description="Chatty API Gateway - Production Ready"

# Install curl for health checks
RUN apk add --no-cache curl

# Create non-root user
RUN addgroup -g 1001 -S spring && \
    adduser -S spring -u 1001

# Set working directory
WORKDIR /app

# Copy JAR from builder stage
COPY --from=builder /app/target/*.jar app.jar

# Change ownership to spring user
RUN chown spring:spring app.jar

# Switch to non-root user
USER spring

# Expose port
EXPOSE 8085

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
  CMD curl -f http://localhost:8085/actuator/health || exit 1

# JVM optimizations for production
ENV JAVA_OPTS="-Xmx512m -Xms256m -XX:+UseG1GC -XX:+UseStringDeduplication"

# Run the application
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar --spring.profiles.active=prod"]
