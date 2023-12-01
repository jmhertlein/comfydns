FROM debian:bookworm-slim

# system prep
ENV TZ America/Chicago
ENV DEBIAN_FRONTEND noninteractive
RUN apt update && apt install -y wget apt-transport-https gawk && mkdir -p /etc/apt/keyrings && wget -O - https://packages.adoptium.net/artifactory/api/gpg/key/public | tee /etc/apt/keyrings/adoptium.asc && echo "deb [signed-by=/etc/apt/keyrings/adoptium.asc] https://packages.adoptium.net/artifactory/deb $(awk -F= '/^VERSION_CODENAME/{print$2}' /etc/os-release) main" | tee /etc/apt/sources.list.d/adoptium.list && apt update && apt install -y temurin-21-jdk maven

# build backend
COPY ./backend /build
WORKDIR /build
RUN mvn clean package


## final image
FROM ruby:3.2-slim

ENV TZ America/Chicago
ENV DEBIAN_FRONTEND noninteractive

RUN apt update && apt install -y wget apt-transport-https gawk && mkdir -p /etc/apt/keyrings && wget -O - https://packages.adoptium.net/artifactory/api/gpg/key/public | tee /etc/apt/keyrings/adoptium.asc && echo "deb [signed-by=/etc/apt/keyrings/adoptium.asc] https://packages.adoptium.net/artifactory/deb $(awk -F= '/^VERSION_CODENAME/{print$2}' /etc/os-release) main" | tee /etc/apt/sources.list.d/adoptium.list && apt update && apt install -y temurin-21-jre maven curl sudo gnupg

# postgres
RUN sh -c 'echo "deb http://apt.postgresql.org/pub/repos/apt bookworm-pgdg main" > /etc/apt/sources.list.d/pgdg.list' && \
    sh -c 'curl "https://www.postgresql.org/media/keys/ACCC4CF8.asc" | apt-key add -' && \
    apt update && apt install -y postgresql-12 && rm -rf /var/lib/postgresql
RUN mkdir -p /opt/comfydns/pg && mkdir -p /opt/comfydns/pgetc && ln -s /opt/comfydns/pg/ /var/lib/postgresql

RUN mkdir -p /static
COPY ./deploy/inabox/pg_hba.conf /static/pg_hba.conf

RUN mkdir /savedetcpg && cp -r /etc/postgresql/* /savedetcpg && rm -rf /etc/postgresql && ln -s /opt/comfydns/pgetc/ /etc/postgresql


# rails

RUN curl -fsSL https://deb.nodesource.com/gpgkey/nodesource-repo.gpg.key | sudo gpg --dearmor -o /etc/apt/keyrings/nodesource.gpg
ARG NODE_MAJOR=18
RUN echo "deb [signed-by=/etc/apt/keyrings/nodesource.gpg] https://deb.nodesource.com/node_$NODE_MAJOR.x nodistro main" | sudo tee /etc/apt/sources.list.d/nodesource.list
RUN apt update -qq
RUN apt-get install -y nodejs ruby-dev build-essential libvips patch zlib1g-dev liblzma-dev nodejs libpq-dev && \
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
