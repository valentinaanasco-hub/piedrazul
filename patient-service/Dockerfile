FROM maven:3.9-eclipse-temurin-21-alpine AS builder
WORKDIR /app
COPY patient-service/pom.xml .
RUN mvn dependency:go-offline -q
COPY patient-service/src ./src
RUN mvn package -DskipTests -q

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=builder /app/target/*.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]
