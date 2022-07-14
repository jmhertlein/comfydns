#!/bin/bash

set -e

yes | docker container prune 
docker build . -t comfydns-axfr:dev -f ./deploy/axfr/Dockerfile
docker run -it --name comfydns-axfr \
-p 127.0.0.1:53:53/tcp \
-p 127.0.0.1:53:53/udp \
-e CDNS_IP=192.168.1.103 \
-e ZONE_NAME=hert \
comfydns-axfr:dev
