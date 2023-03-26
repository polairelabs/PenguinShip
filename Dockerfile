# Build stage
FROM maven:3-openjdk-17-slim AS build
COPY src /home/app/src
COPY pom.xml /home/app/pom.xml
RUN mvn -f /home/app/pom.xml clean package

# Package stage
FROM openjdk:17-alpine
COPY --from=build /home/app/target/api.jar /usr/local/lib/api.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/usr/local/lib/api.jar"]