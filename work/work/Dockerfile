# Build stage: compile the Spring Boot app inside the container (openjdk pattern)
# openjdk:* images are deprecated on Docker Hub; eclipse-temurin is the official OpenJDK build
FROM eclipse-temurin:17-jdk AS build
COPY . /usr/src/myapp
WORKDIR /usr/src/myapp
RUN chmod +x mvnw && ./mvnw -q -DskipTests package

# Runtime stage: run only the built jar
FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /usr/src/myapp/target/*.jar app.jar
EXPOSE 8080
# -XX:MaxRAMPercentage makes the JVM respect the container RAM limit
CMD ["java", "-XX:MaxRAMPercentage=75.0", "-jar", "app.jar"]
