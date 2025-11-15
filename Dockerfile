# Multi-stage Docker build for Java 21 Spring Boot application
FROM maven:3.9.5-eclipse-temurin-21 AS builder

WORKDIR /app

# Copy parent pom first
COPY pom.xml .

# Copy all module poms
COPY platform-common/pom.xml platform-common/
COPY platform-core/pom.xml platform-core/
COPY platform-api/pom.xml platform-api/

# Download dependencies (this layer will be cached if poms don't change)
RUN mvn dependency:go-offline -B

# Copy source code
COPY platform-common/src platform-common/src
COPY platform-core/src platform-core/src
COPY platform-api/src platform-api/src

# Build the application
RUN mvn clean package -DskipTests -B

# Runtime stage
FROM eclipse-temurin:21-jre-jammy

WORKDIR /app

# Create non-root user
RUN groupadd -r platform && useradd -r -g platform platform

# Copy the built JAR from builder stage
COPY --from=builder /app/platform-api/target/*.jar app.jar

# Change ownership
RUN chown -R platform:platform /app

# Switch to non-root user
USER platform

# Expose port
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
  CMD curl -f http://localhost:8080/actuator/health || exit 1

# Run the application
ENTRYPOINT ["java", "-XX:+UseContainerSupport", "-XX:MaxRAMPercentage=75.0", "-jar", "app.jar"]
