#!/bin/bash

set -e

yes | docker container prune 
docker build . -t comfydns:dev -f ./deploy/byopg/Dockerfile
docker run -it --name comfydns \
-v comfydns-data-byopg:/opt/comfydns/ \
-p 127.0.0.1:53:53/tcp \
-p 127.0.0.1:33200:33200/tcp \
-p 127.0.0.1:53:53/udp \
-p 127.0.0.1:8080:3000/tcp \
-e COMFYDNS_UI_PASSPHRASE=test \
-e USAGE_REPORTING_DISABLED=1 \
-e CDNS_DB_NAME=comfydns-container \
-e CDNS_DB_HOST=172.17.0.1 \
comfydns:dev
