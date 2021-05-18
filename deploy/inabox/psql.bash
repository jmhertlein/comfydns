#!/bin/bash


cd /app/ui
# disable x so we don't leak secret into logs
set +x
if [[ -f /opt/comfydns/rails_secret.txt ]]; then
    echo "Using existing rails secret_key_base"
    export SECRET_KEY_BASE="$(cat /opt/comfydns/rails_secret.txt)"
else
    echo "Generating new rails secret_key_base"
    export SECRET_KEY_BASE="$(bin/rails secret)"
    echo "$SECRET_KEY_BASE" > /opt/comfydns/rails_secret.txt
fi

bin/rails db
