# CuraNexus - Multi-module Dockerfile
# Usage: docker build --build-arg MODULE_NAME=patient -t curanexus/patient .

FROM eclipse-temurin:21-jre-alpine

# Build arguments
ARG MODULE_NAME
ARG JAR_FILE=modules/${MODULE_NAME}/target/*.jar

# Labels for image metadata
LABEL maintainer="CuraNexus Team"
LABEL org.opencontainers.image.source="https://github.com/curanexus/curanexus"
LABEL org.opencontainers.image.description="CuraNexus ${MODULE_NAME} module"

# Install curl for healthcheck and create non-root user
RUN apk add --no-cache curl && \
    addgroup -g 1001 -S curanexus && \
    adduser -u 1001 -S curanexus -G curanexus

# Set working directory
WORKDIR /app

# Copy the JAR file
COPY ${JAR_FILE} app.jar

# Change ownership to non-root user
RUN chown -R curanexus:curanexus /app

# Switch to non-root user
USER curanexus

# Default port (can be overridden)
EXPOSE 8080

# JVM options for containers
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -Djava.security.egd=file:/dev/./urandom"

# Health check - uses SERVER_PORT env var, defaults to 8080
HEALTHCHECK --interval=30s --timeout=10s --start-period=90s --retries=3 \
    CMD curl -sf http://localhost:${SERVER_PORT:-8080}/actuator/health || exit 1

# Start the application
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]
