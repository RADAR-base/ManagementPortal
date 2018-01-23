#!/bin/bash

# wait for app to start up
echo "Waiting for application startup"
until curl -s http://localhost:8080/managementportal/ > /dev/null
do
echo -n "." # waiting
sleep 2
done
echo ""
echo "Application is up"
