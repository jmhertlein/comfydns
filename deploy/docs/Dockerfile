FROM ubuntu:focal

# system prep
ENV TZ America/Chicago
ENV DEBIAN_FRONTEND noninteractive
RUN apt update && apt install -y pipenv python3

# mk hier 
RUN mkdir -p /build /app /app/docs /build/docs

COPY ./docs/Pipfile* /build/docs/
WORKDIR /build/docs
RUN pipenv sync

COPY ./docs /build/docs
WORKDIR /build/docs
RUN pipenv run sphinx-build -b html ./source/ ./build/
RUN cp -r /build/docs/build/* /app/docs/

WORKDIR /app
CMD rm -rf /app/public/* && cp -r /app/docs/* /app/public && sleep infinity
