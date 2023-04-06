# Build stage
FROM maven:3-openjdk-17-slim AS build
COPY src /home/app/src
COPY pom.xml /home/app/pom.xml
COPY wait-for-it.sh /home/app/wait-for-it.sh
RUN chmod +x /home/app/wait-for-it.sh
RUN mvn -f /home/app/pom.xml clean package

# Package stage
FROM openjdk:17-alpine
RUN apk add --no-cache bash
COPY --from=build /home/app/target/api.jar /usr/local/lib/api.jar
COPY --from=build /home/app/wait-for-it.sh /usr/local/bin/wait-for-it.sh
EXPOSE 8080
ENTRYPOINT ["/usr/local/bin/wait-for-it.sh", "db:5432", "--", "java", "-jar", "/usr/local/lib/api.jar"]
