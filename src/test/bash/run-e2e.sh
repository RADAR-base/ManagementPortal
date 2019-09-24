#!/bin/bash

# do not run e2e tests on travis 'tag' builds
if [ -z $TRAVIS_TAG ]
then
  echo "Running e2e tests"
  yarn webdriver-manager update --versions.chrome 77.0.3865.10
  yarn e2e
else
  echo "Skipping e2e tests on tag builds"
fi
