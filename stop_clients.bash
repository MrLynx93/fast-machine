#!/usr/bin/env bash

echo 'killing mqtt_client processes:'
ps aux | awk '{print $11" "$2}' | grep '^./test'
ps aux | awk '{print $11" "$2}' | grep '^./test' | awk '{print $2}' | xargs kill -9

echo 'killing coap_client processes:'
netstat -tulpn | awk '{print $4" "$6}' | grep 290 | awk '{print $2 }' | awk -F'/' '{print $1}'
netstat -tulpn | awk '{print $4" "$6}' | grep 290 | awk '{print $2 }' | awk -F'/' '{print $1}' | xargs kill -9

echo 'killing coap_server process:'
netstat -tulpn | awk '{print $4" "$6}' | grep 19001 | awk '{print $2 }' | awk -F'/' '{print $1}'
netstat -tulpn | awk '{print $4" "$6}' | grep 19001 | awk '{print $2 }' | awk -F'/' '{print $1}' | xargs kill -9