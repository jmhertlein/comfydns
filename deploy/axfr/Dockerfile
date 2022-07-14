FROM ubuntu:focal

# system prep
ENV TZ America/Chicago
ENV DEBIAN_FRONTEND noninteractive
RUN apt update && apt install -y bind9

RUN mkdir /app
COPY ./deploy/axfr/entrypoint.bash /app
COPY ./deploy/axfr/named.conf.template /app

WORKDIR /app
RUN chmod +x /app/entrypoint.bash
CMD ./entrypoint.bash
