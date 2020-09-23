# Docker multi-stage build

# 1. Building the App with Maven
FROM maven:3-jdk-11

ADD . /developer-tracker
WORKDIR /developer-tracker

# Just echo so we can see, if everything is there :)
RUN ls -l

# Run Maven build
RUN mvn clean install


# Just using the build artifact and then removing the build-container
FROM openjdk:11-jdk

MAINTAINER Matheus Ferreira

VOLUME /tmp

# Add Spring Boot app.jar to Container
COPY --from=0 "/developer-tracker/backend/target/backend-0.0.1-SNAPSHOT.jar" app.jar
COPY --from=0 "/developer-tracker/backend/cloc-tool/cloc-1.86.pl" cloc-1.86.pl
COPY --from=0 "/developer-tracker/backend/truckfactor-tool" /truckfactor-tool

ENV JAVA_OPTS=""

# Fire up our Spring Boot app by default
ENTRYPOINT [ "sh", "-c", "java $JAVA_OPTS -Djava.security.egd=file:/dev/./urandom -jar /app.jar" ]
