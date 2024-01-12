#FROM openjdk:8-jdk-alpine
#MAINTAINER ee.ciszewsj
#
#WORKDIR /app
#
#COPY .mvn/ .mvn
#COPY mvnw pom.xml ./
#RUN ./mvnw dependency:go-offline
#COPY src ./src
#
#
#COPY target/Secure-Application-0.0.1-SNAPSHOT.jar Secure-Application-0.0.1-SNAPSHOT.jar
#ENTRYPOINT ["java","-jar","/Secure-Application-0.0.1-SNAPSHOT.jar"]
#
FROM eclipse-temurin:17-jdk-focal

WORKDIR /app

COPY .mvn/ .mvn
COPY mvnw pom.xml ./

RUN apt-get update && \
    apt-get install -y maven && \
    mvn -N io.takari:maven:wrapper


RUN ./mvnw dependency:go-offline

COPY src ./src

CMD ["./mvnw", "spring-boot:run"]
