#!/bin/bash

# Test the docker build on release branches only. Because our command is running in an if statement,
# we need to put this in it's own file, since Travis will only see the exit status of the if
# statement.

# only run on the release branch and master branch if it's not a tag build
if [[ $TRAVIS_BRANCH == release-* || ($TRAVIS_BRANCH == master && -z $TRAVIS_TAG) ]]
then
  echo "Testing docker image build"
  docker build -t managementportal .
  ./gradlew bootRepackage -Pprod buildDocker -x test
else
  echo "Skipping building docker image"
fi
