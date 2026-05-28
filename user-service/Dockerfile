# Stage 1 — Build: compila il progetto con Maven
FROM maven:3.9.6-eclipse-temurin-17-alpine AS build_stage
WORKDIR /build
# Copia il pom separatamente per sfruttare il layer cache di Docker:
# se solo il sorgente cambia, le dipendenze non vengono riscaricate
COPY pom.xml .
RUN mvn dependency:go-offline -q
COPY src ./src
RUN mvn clean package -DskipTests -q

# Stage 2 — Runtime: immagine minimale con solo il JRE
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build_stage /build/target/user-service-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
