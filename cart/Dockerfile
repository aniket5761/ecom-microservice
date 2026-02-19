#java 17 runtime
FROM eclipse-temurin:17-jdk-jammy

# Set working directory inside container
WORKDIR /app

# Copy your built jar into container
COPY target/cart.jar app.jar

# Expose port
EXPOSE 8086

# Command to run your application
ENTRYPOINT ["java", "-jar", "app.jar"]