#!/bin/bash

# Test the docker build on release branches only. Because our command is running in an if statement,
# we need to put this in it's own file, since Travis will only see the exit status of the if
# statement.

set -ev

# only run on the release branch's push and pull_request events
if [[ $TRAVIS_BRANCH == release-* ]] || [[ $TRAVIS_PULL_REQUEST_BRANCH == release-* ]]
then
  echo "Testing docker image build"
  docker build -t managementportal .
  ./gradlew bootRepackage -Pprod buildDocker -x test
else
  echo "Skipping building docker image"
fi
