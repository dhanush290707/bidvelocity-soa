# Build any BidVelocity Maven module from the backend root. Compose supplies
# SERVICE (for example, "auction-service") for each container image.
FROM maven:3.9.9-eclipse-temurin-21 AS build

WORKDIR /workspace
COPY . .

ARG SERVICE
RUN test -n "$SERVICE" && mvn -pl "$SERVICE" -am package -DskipTests

FROM eclipse-temurin:21-jre

WORKDIR /app
ARG SERVICE
COPY --from=build /workspace/${SERVICE}/target/*.jar /app/app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
