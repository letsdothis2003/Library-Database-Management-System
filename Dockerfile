# Stage 1: Build the application
FROM eclipse-temurin:17-jdk-jammy as build
WORKDIR /app

# Copy everything first to ensure we have the full structure
# The previous error occurred because 'src' could not be found independently
COPY . .

# Grant execute permission
RUN chmod +x mvnw

# Build the JAR
# We skip 'dependency:go-offline' for now to see the direct error if it fails
RUN ./mvnw clean package -DskipTests

# Stage 2: Run the application
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app

# Use a wildcard to find the generated JAR file
COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]