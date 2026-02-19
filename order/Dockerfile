#Java 17 runtime
FROM eclipse-temurin:17-jdk-jammy

# Set working directory inside container
WORKDIR /app

# Copy your built jar into container
COPY target/order.jar app.jar

# Expose port
EXPOSE 8084

# Command to run your application
ENTRYPOINT ["java", "-jar", "app.jar"]

