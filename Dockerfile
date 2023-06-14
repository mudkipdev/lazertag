FROM eclipse-temurin:17-jre-alpine

RUN mkdir /app
WORKDIR /app

COPY build/libs/*-all.jar /app/lazertag.jar
COPY run/maps/ /app/maps/

CMD ["java", "-jar", "/app/lazertag.jar"]

