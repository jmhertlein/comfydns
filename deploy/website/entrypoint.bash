#!/bin/bash

set -x
set -e

service postgresql start

cd /app/website
# disable x so we don't leak secret into logs
set +x
echo "Generating new rails secret_key_base"
export SECRET_KEY_BASE="$(bundle exec rails secret)"
echo "$SECRET_KEY_BASE" > /opt/comfydns/rails_secret.txt
set -x

bundle exec rails db:create db:migrate

test -e /app/website/nginxmnt && rm -r /app/website/nginxmnt/*
mkdir -p /app/website/nginxmnt
cp -r /app/website/public/* /app/website/nginxmnt

bundle exec rails server
