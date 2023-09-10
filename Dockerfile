FROM --platform=$BUILDPLATFORM eclipse-temurin:20-jre

RUN mkdir /app
WORKDIR /app

COPY build/libs/*-all.jar /app/lazertag.jar
COPY run/maps/ /app/maps/

CMD ["java", "-jar", "/app/lazertag.jar"]

