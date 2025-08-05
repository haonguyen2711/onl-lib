# Build stage
FROM eclipse-temurin:21-jdk-ubi10-minimal AS builder

WORKDIR /app

# Copy Maven Wrapper and project files
COPY mvnw .
COPY mvnw.cmd .
COPY .mvn ./.mvn
COPY pom.xml .
COPY src ./src

# Make Maven Wrapper executable and build
RUN chmod +x mvnw && \
    ./mvnw clean package -DskipTests

# Runtime stage
FROM eclipse-temurin:21-jdk-ubi10-minimal

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
