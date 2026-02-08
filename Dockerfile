#STAGE 1: Build the Application  
FROM eclipse-temurin:17-jdk-jammy as build
WORKDIR /app

# 1. Copy everything to ensure the build environment sees all folders
# This copies from the root of your GitHub repo into the /app folder in Docker
COPY . .

# 2. Grant execute permissions to the Maven wrapper
# This is crucial for Railway to run the build command
RUN chmod +x mvnw

# 3. Build the JAR
# -B: Batch mode (less log noise)
# -DskipTests: Skip testing to speed up cloud deployment
# -Dstart-class: Explicitly tells Maven which class has the main method
RUN ./mvnw clean package -B -DskipTests -Dstart-class=WebDemonstration.Frontend

# --- STAGE 2: Run the Application ---
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app

# 4. Copy the compiled JAR from the build stage
# Maven always puts the output in the 'target' folder
COPY --from=build /app/target/*.jar app.jar

# 5. Inform Railway about the port (Standard Spring Boot)
EXPOSE 8080

# 6. Start the app
ENTRYPOINT ["java", "-jar", "app.jar"]
