# Use official Java 21 runtime (small & optimized)
FROM eclipse-temurin:17-jdk-jammy

# Set working directory inside container
WORKDIR /app

# Copy your built jar into container
COPY target/inventory.jar app.jar

# Expose port
EXPOSE 8083

# Command to run your application
ENTRYPOINT ["java", "-jar", "app.jar"]
