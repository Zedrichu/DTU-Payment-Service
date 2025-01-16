# OpenJDK runtime base image
FROM eclipse-temurin:23 AS jre-build
# Set working directory inside container
WORKDIR /service

ENV LANGUAGE="en_US:en"

#COPY target/*-runner.jar /service/quarkus-run.jar

# Copy the compiled service runner
COPY target/quarkus-app /service/quarkus-app

# Expose container port
EXPOSE 8080

ENV JAVA_OPTS_APPEND="-Dquarkus.http.host=0.0.0.0 -Djava.util.logging.manager=org.jboss.logmanager.LogManager"

#ENV JAVA_APP_JAR="/deployments/quarkus-run.jar"
#ENTRYPOINT [ "/opt/jboss/container/java/run/run-java.sh" ]

# Command to run the application server on start-up
CMD ["java", "-Xmx64m", "-jar", "quarkus-app/quarkus-run.jar"]
#ENTRYPOINT [ "java -Xmx64m -jar quarkus-app/quarkus-run.jar"]