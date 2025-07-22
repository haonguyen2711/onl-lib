# Build stage
FROM openjdk:21-jdk-slim as builder

WORKDIR /app
COPY pom.xml .
COPY src ./src

# Install Maven
RUN apt-get update && apt-get install -y maven

# Build application
RUN mvn clean package -DskipTests

# Runtime stage
FROM openjdk:21-jre-slim

WORKDIR /app

# Create directories
RUN mkdir -p storage/books storage/temp keys logs

# Copy application
COPY --from=builder /app/target/onllib-1.0-SNAPSHOT.jar app.jar

# Create non-root user
RUN groupadd -r onllib && useradd -r -g onllib onllib
RUN chown -R onllib:onllib /app
USER onllib

# Expose port
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=40s --retries=3 \
  CMD curl -f http://localhost:8080/api/actuator/health || exit 1

# Run application
ENTRYPOINT ["java", "-jar", "app.jar"]
