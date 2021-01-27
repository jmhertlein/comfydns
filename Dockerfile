FROM ubuntu:focal

ENV TZ America/Chicago
ENV DEBIAN_FRONTEND noninteractive
RUN apt update && apt install -y openjdk-11-jdk-headless maven

RUN mkdir /build /app

COPY ./recursor /build/recursor

WORKDIR /build/recursor
RUN mvn clean package
RUN bash -c "cp /build/recursor/target/comfydns-recursor-*.jar /app/recursor.jar"

CMD ["java", "-jar", "/app/recursor.jar"]
