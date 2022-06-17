#Start with a base image containing Java runtime
FROM openjdk:11

VOLUME /tmp

ENV JAVA_TOOL_OPTIONS -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005

ARG JAR_FILE

#Add the application's jar to the container
COPY ${JAR_FILE} app.jar

#execute the application
ENTRYPOINT ["java", "-Djava.security.egd=file:/dev/./urandom", "-jar", "/app.jar"]