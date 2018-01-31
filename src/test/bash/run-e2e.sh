#!/bin/bash

# do not run e2e tests on travis 'tag' builds
if [ -z $TRAVIS_TAG ]
then
  echo "Running e2e tests"
  yarn e2e
else
  echo "Skipping e2e tests on tag builds"
fi
