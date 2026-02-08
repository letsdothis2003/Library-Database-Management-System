# Stage 1: Build the application
FROM eclipse-temurin:17-jdk-jammy as build
WORKDIR /app

# Copy the Maven wrapper and project files
COPY . .

# Grant execute permission and build the JAR
RUN chmod +x mvnw && ./mvnw clean package -DskipTests

# Stage 2: Run the application
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app

# Copy the compiled JAR from the build stage
COPY --from=build /app/target/*.jar app.jar

# Expose the standard Spring Boot port
EXPOSE 8080

# Start the application
ENTRYPOINT ["java", "-jar", "app.jar"]