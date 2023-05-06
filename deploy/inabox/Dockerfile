FROM debian:bullseye-slim

# system prep
ENV TZ America/Chicago
ENV DEBIAN_FRONTEND noninteractive
RUN apt update && apt install -y openjdk-11-jdk-headless maven

# build backend
COPY ./backend /build
WORKDIR /build
RUN mvn clean package


## final image
FROM ruby:3.2-slim

ENV TZ America/Chicago
ENV DEBIAN_FRONTEND noninteractive

RUN apt update && apt install -y openjdk-11-jre-headless curl sudo gnupg

# postgres
RUN sh -c 'echo "deb http://apt.postgresql.org/pub/repos/apt bullseye-pgdg main" > /etc/apt/sources.list.d/pgdg.list' && \
    sh -c 'curl "https://www.postgresql.org/media/keys/ACCC4CF8.asc" | apt-key add -' && \
    apt update && apt install -y postgresql-12 && rm -rf /var/lib/postgresql
RUN mkdir -p /opt/comfydns/pg && mkdir -p /opt/comfydns/pgetc && ln -s /opt/comfydns/pg/ /var/lib/postgresql

RUN mkdir -p /static
COPY ./deploy/inabox/pg_hba.conf /static/pg_hba.conf

RUN mkdir /savedetcpg && cp -r /etc/postgresql/* /savedetcpg && rm -rf /etc/postgresql && ln -s /opt/comfydns/pgetc/ /etc/postgresql


# rails
ARG NODE_MAJOR_VERSION=18
RUN curl -sL https://deb.nodesource.com/setup_$NODE_MAJOR_VERSION.x | bash -
RUN apt update -qq
RUN apt-get install -y ruby-dev build-essential libvips patch zlib1g-dev liblzma-dev nodejs libpq-dev && \
    apt-get clean && \
    rm -rf /var/lib/apt/lists/* /usr/share/doc /usr/share/man && \
    npm install -g yarn

# rails-deps
RUN mkdir -p /app/ui
COPY ./ui/Gemfile* /app/ui/
WORKDIR /app/ui
RUN gem install bundler
RUN bundle install --deployment --without development test
ENV RAILS_ENV production

# rails-app
COPY ./ui /app/ui
ENV RAILS_SERVE_STATIC_FILES true
ENV SECRET_KEY_BASE placeholder
RUN bundle exec rake assets:precompile
ENV SECRET_KEY_BASE=

# install recursor backend
COPY --from=0 /build/recursor/target/comfydns-recursor-*.jar /app/recursor.jar

# set up supervisor
RUN apt update && apt install -y supervisor
COPY ./deploy/inabox/supervisord.conf /etc/supervisor/conf.d/supervisord.conf
COPY ./deploy/inabox/entrypoint.rb /app
COPY ./deploy/inabox/psql.bash /app
RUN chmod +x /app/entrypoint.rb
RUN chmod +x /app/psql.bash

ENV CDNS_ROOT_PATH /opt/comfydns/

WORKDIR /app/ui
CMD bundle exec ruby ../entrypoint.rb