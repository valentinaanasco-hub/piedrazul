FROM maven:3.9-eclipse-temurin-21-alpine AS builder
WORKDIR /app

# Compilar shared-events primero
COPY shared-events/pom.xml shared-events/pom.xml
RUN mvn dependency:go-offline -f shared-events/pom.xml -q
COPY shared-events/src shared-events/src
RUN mvn install -f shared-events/pom.xml -DskipTests -q

# Compilar medical-staff-service
COPY medical-staff-service/pom.xml .
RUN mvn dependency:go-offline -q
COPY medical-staff-service/src ./src
RUN mvn package -DskipTests -q

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=builder /app/target/*.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]
