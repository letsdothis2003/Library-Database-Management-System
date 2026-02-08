# --- STAGE 1: Build the Application ---
FROM eclipse-temurin:17-jdk-jammy as build
WORKDIR /app

# 1. Copy everything to ensure the build environment sees all folders
COPY . .

# 2. Grant execute permissions to the Maven wrapper
RUN chmod +x mvnw

# 3. Build the JAR
# We removed the hardcoded -Dstart-class to let Maven find the @SpringBootApplication automatically
RUN ./mvnw clean package -B -DskipTests

# --- STAGE 2: Run the Application ---
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app

# 4. Copy the compiled JAR from the build stage
COPY --from=build /app/target/*.jar app.jar

# 5. Inform Railway about the port
EXPOSE 8080

# 6. Start the app
ENTRYPOINT ["java", "-jar", "app.jar"]
