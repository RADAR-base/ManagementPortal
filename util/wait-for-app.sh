#!/bin/bash

if [ -z $1 ]
then
  echo "No URL specified, using http://localhost:8080/"
  APP_URL='http://localhost:8080/'
else
  APP_URL=$1
fi

# wait for app to start up
echo "Waiting for application startup"
until curl -s $APP_URL > /dev/null
do
  echo -n "." # waiting
  sleep 2
done
echo ""
echo "Application is up"
