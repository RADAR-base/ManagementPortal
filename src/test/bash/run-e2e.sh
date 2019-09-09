#!/bin/bash

# do not run e2e tests on travis 'tag' builds
if [ -z $TRAVIS_TAG ]
then
  echo "Running e2e tests"
  ls -al node_modules/webdriver-manager/selenium/
  yarn webdriver-manager update
  yarn e2e
else
  echo "Skipping e2e tests on tag builds"
fi
