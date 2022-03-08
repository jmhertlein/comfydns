FROM ubuntu:focal

# system prep
ENV TZ America/Chicago
ENV DEBIAN_FRONTEND noninteractive
RUN mkdir -p /opt/comfydns/pg && ln -s /opt/comfydns/pg/ /var/lib/postgresql
RUN apt update && apt install -y ruby ruby-dev build-essential patch zlib1g-dev liblzma-dev libsqlite3-dev sqlite3 nginx nodejs yarnpkg openjdk-11-jdk-headless maven supervisor libpq-dev vim jq postgresql uuid-runtime

# mk hier 
RUN mkdir -p /build /app /app/ui /build/backend /build/backend/recursor /build/backend/runner /build/backend/util

# setup db
COPY ./deploy/inabox/pg_hba.conf /etc/postgresql/12/main/pg_hba.conf
RUN service postgresql start && su postgres -c 'createuser -w -s comfydns' && service postgresql stop

# early resolve of dependencies for better build caching
COPY ./ui/Gemfile* /app/ui/
COPY ./ui/bin /app/ui/bin
WORKDIR /app/ui
RUN gem install bundler
ENV RAILS_ENV production
RUN ./bin/bundle install --without development test

COPY ./backend/pom.xml /build/backend
COPY ./backend/recursor/pom.xml /build/backend/recursor
WORKDIR /build/backend/
RUN mvn dependency:go-offline


# ui is slower, do it first
COPY ./ui /app/ui
WORKDIR /app/ui
RUN ln /usr/bin/yarnpkg /usr/bin/yarn
ENV RAILS_SERVE_STATIC_FILES true
ENV SECRET_KEY_BASE placeholder
RUN bundle exec rake assets:precompile
ENV SECRET_KEY_BASE=

# build backend
COPY ./backend /build/backend
WORKDIR /build/backend
RUN mvn clean package
RUN bash -c "cp /build/backend/recursor/target/comfydns-recursor-*.jar /app/recursor.jar"


# set up supervisor
COPY ./deploy/inabox/supervisord.conf /etc/supervisor/conf.d/supervisord.conf
COPY ./deploy/inabox/entrypoint.bash /app
COPY ./deploy/inabox/psql.bash /app
RUN chmod +x /app/entrypoint.bash
RUN chmod +x /app/psql.bash

ENV CDNS_ROOT_PATH /opt/comfydns/

WORKDIR /app
CMD ./entrypoint.bash
