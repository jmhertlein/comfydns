#!/bin/bash

set -e

yes | docker container prune 
docker build . -t comfydns-web:dev -f ./deploy/website/Dockerfile
docker run -it --name comfydns \
-v comfydns-website-data:/opt/comfydns/ \
-e RAILS_SERVE_STATIC_FILES=true \
-p 127.0.0.1:80:3000/tcp \
comfydns-web:dev
