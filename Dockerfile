FROM openjdk:17.0.2-jdk

ADD . /reactive-country-api
WORKDIR /reactive-country-api

RUN ./gradlew clean build

ENTRYPOINT ["java", "-jar","./application/build/libs/application-0.0.1-SNAPSHOT.jar"]

EXPOSE 8080
