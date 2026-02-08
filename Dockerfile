# Stage 1: Build the application
FROM eclipse-temurin:17-jdk-jammy as build
WORKDIR /app

# Copy Maven wrapper and pom first to cache dependencies
COPY .mvn/ .mvn
COPY mvnw pom.xml ./
RUN chmod +x mvnw
# Pre-download dependencies to save time and prevent timeouts
RUN ./mvnw dependency:go-offline

# Copy the rest of the source code and build
COPY src ./src
RUN ./mvnw clean package -DskipTests

# Stage 2: Run the application
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app

COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]