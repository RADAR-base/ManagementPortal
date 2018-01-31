#!/bin/bash

if [ -z $1 ]
then
  echo "No URL specified, using http://localhost:8080/"
  APP_URL='http://localhost:8080/'
else
  APP_URL=$1
fi

# wait for app to start up
echo "Waiting for application startup at $APP_URL"
RETRY_COUNT=0
until curl -s $APP_URL > /dev/null
do
  echo -n "." # waiting
  sleep 2
  ((RETRY_COUNT++))
  if ((RETRY_COUNT > 120)) # wait max 120 times, so max 4 minutes
  then
    echo ""
    echo "Application failed to start"
    exit 1
  fi
done
echo ""
echo "Application is up"
