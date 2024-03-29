FROM ubuntu:jammy

# system prep
ENV TZ America/Chicago
ENV DEBIAN_FRONTEND noninteractive
RUN mkdir -p /opt/comfydns/pg && ln -s /opt/comfydns/pg/ /var/lib/postgresql
RUN apt update && apt install -y rbenv ruby ruby-dev build-essential patch zlib1g-dev liblzma-dev libsqlite3-dev sqlite3 nginx nodejs yarnpkg supervisor libpq-dev vim jq postgresql uuid-runtime

# mk hier 
RUN mkdir -p /build /app /app/website 

# setup db
COPY ./deploy/website/pg_hba.conf /etc/postgresql/14/main/pg_hba.conf
RUN service postgresql start && su postgres -c 'createuser -w -s comfydns' && service postgresql stop

COPY ./website /app/website
WORKDIR /app/website
RUN gem install bundler:2.4.1
RUN bundle install --deployment --without development test
RUN ln /usr/bin/yarnpkg /usr/bin/yarn
ENV RAILS_ENV production
ENV SECRET_KEY_BASE placeholder
RUN bundle exec rails assets:precompile
ENV SECRET_KEY_BASE=

# set up supervisor
COPY ./deploy/website/entrypoint.bash /app
COPY ./deploy/website/psql.bash /app
RUN chmod +x /app/entrypoint.bash
RUN chmod +x /app/psql.bash

WORKDIR /app
CMD ./entrypoint.bash
