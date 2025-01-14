# OpenJDK runtime base image
FROM eclipse-temurin:23 as jre-build
# Set working directory inside container
WORKDIR /usr/src
COPY target/quarkus-app /usr/src/quarkus-app
# Command to run the application server
CMD ["java", "-Xmx64m", "-jar", "quarkus-app/quarkus-run.jar"]
