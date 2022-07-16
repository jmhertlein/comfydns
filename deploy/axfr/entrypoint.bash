#!/bin/bash

set -x
set -e
cd /app

cat /app/named.conf.template | sed "s/PH_MASTER_IP/$CDNS_IP/g" | sed "s/PH_ZONE_NAME_NO_LEADING_DOT/$ZONE_NAME/g" \
| sed "s^PH_RECURSION_FOR_SUBNET^$ALLOW_RECURSION_FOR_SUBNET^g" > named.conf

echo "===START CONFIG================="
cat named.conf
echo "===END CONFIG==================="
named -4gc /app/named.conf
