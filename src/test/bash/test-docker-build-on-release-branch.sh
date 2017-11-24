#!/bin/bash

# Test the docker build on release branches only. Because our command is running in an if statement,
# we need to put this in it's own file, since Travis will only see the exit status of the if
# statement.

set -ev

if [[ $TRAVIS_BRANCH == add-production-* ]]
then
  docker build -t managementportal .
fi
