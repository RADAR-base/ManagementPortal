#!/bin/bash

# For testing production mode e2e tests we need some tweaks:
# 1) The angular app needs to run in dev mode for protractor to work
# 2) We update the basehref of the angular app to the context path of the backend
# 3) We update the protractor configuration to the new path
#
# Then we start up a docker stack with a postgres server since production mode is configured for a
# postgres database instead of in-memory database.

# only run on the release branch and master branch if it's not a tag build
if [[ $TRAVIS_BRANCH == release-* || ($TRAVIS_BRANCH == master && -z $TRAVIS_TAG) ]]
then
  echo "Running production e2e tests"
  sed -i "s|new plugin.BaseHrefWebpackPlugin({ baseHref: '/' })|new plugin.BaseHrefWebpackPlugin({ baseHref: '/managementportal/' })|" webpack/webpack.dev.js
  sed -i "s|baseUrl: 'http://localhost:8080/',|baseUrl: 'http://localhost:8080/managementportal/',|" src/test/javascript/protractor.conf.js
  sed -i "s|contexts: prod|contexts: dev|" src/main/resources/config/application-prod.yml # set liquibase context to dev so it loads demo data
  ./gradlew bootRepackage -Pprod buildDocker -x test
  docker-compose -f src/main/docker/app.yml up -d # spin up production mode application
  # wait for app to be up
  $TRAVIS_BUILD_DIR/util/wait-for-app.sh http://localhost:8080/managementportal/
  docker-compose -f src/main/docker/app.yml logs # show output of app startup
  yarn webdriver-manager update --versions.chrome "$(google-chrome --version | grep -iE "[0-9.]{10,20}")"
  yarn e2e # run e2e tests against production mode
  docker-compose -f src/main/docker/app.yml down -v # clean up containers and volumes
  git checkout src/test/javascript/protractor.conf.js
  git checkout webpack/webpack.prod.js
  git checkout src/main/resources/config/application-prod.yml
else
  echo "Skipping production e2e tests"
fi
