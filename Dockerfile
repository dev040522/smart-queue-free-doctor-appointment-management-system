FROM maven:3.9.11-eclipse-temurin-17 AS build

WORKDIR /app

COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
RUN chmod +x mvnw

COPY src/ src/
RUN ./mvnw -q -DskipTests package

FROM eclipse-temurin:17-jre

WORKDIR /app

# Render can mount a persistent disk here for the H2 database.
RUN mkdir -p /app/data

COPY --from=build /app/target/doctor-appointment-systemqueue-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8080

ENTRYPOINT ["sh", "-c", "java -jar app.jar"]
