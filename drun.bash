#!/bin/bash

set -e

yes | docker container prune 
docker build . -t comfydns:dev
docker run -it --name comfydns \
-p 127.0.0.1:53:53/tcp \
-p 127.0.0.1:33200:33200/tcp \
-p 127.0.0.1:53:53/udp \
comfydns:dev
