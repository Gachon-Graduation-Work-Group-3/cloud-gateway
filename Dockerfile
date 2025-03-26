FROM openjdk:21
ARG JAR_FILE=build/libs/whenyourcar-gateway-0.0.1-SNAPSHOT.jar
COPY ${JAR_FILE} app.jar
ENV SPRING_PROFILES_ACTIVE=deploy

ENTRYPOINT ["java", "-jar", "app.jar"]
