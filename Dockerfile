# Use Maven to build the project
FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

# Use a minimal runtime image for the final container
FROM eclipse-temurin:17-jdk-alpine
WORKDIR /app

# Copy the built jar from the build stage
COPY --from=build /app/target/patient-pdf-api-1.0.jar app.jar

# Run the Spring Boot application
ENTRYPOINT ["java", "-jar", "app.jar"]
