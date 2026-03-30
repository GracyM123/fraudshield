# Use a full JDK image with Maven pre-installed
FROM maven:3.9.6-eclipse-temurin-17-alpine AS build
WORKDIR /app

# Copy pom.xml first and download dependencies as a separate cached layer
COPY pom.xml .

# Download dependencies only (cached if pom.xml unchanged)
RUN mvn dependency:resolve -q --no-transfer-progress

# Copy source and build
COPY src ./src
RUN mvn clean package -DskipTests -q --no-transfer-progress

# Runtime image - much smaller
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /app/target/fraud-detection-backend-1.0.0.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", \
  "-XX:+UseContainerSupport", \
  "-XX:MaxRAMPercentage=75.0", \
  "-jar", "app.jar"]
