# Build stage
FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
COPY api/pom.xml api/
COPY core/pom.xml core/
COPY infrastructure/pom.xml infrastructure/
COPY bootstrap/pom.xml bootstrap/
RUN mvn dependency:go-offline

COPY . .
RUN mvn clean package -DskipTests

# Run stage
FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /app/bootstrap/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
