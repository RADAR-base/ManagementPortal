#!/bin/bash

# For testing production mode e2e tests we need some tweaks:
# 1) The context path is at /managementportal so we need to modify protractor.conf.js
# 2) The angular app still needs to run in dev mode for protractor to work
#
# Then we start up a docker stack with a postgres server since production mode is configured for a
# postgres database instead of in-memory database.

set -ev

sed -i 's|http://localhost:8080/|http://localhost:8080/managementportal/|' src/test/javascript/protractor.conf.js
sed -i "s/DEBUG_INFO_ENABLED: options.env === 'dev'/DEBUG_INFO_ENABLED: true/" webpack/webpack.common.js
grep baseUrl src/test/javascript/protractor.conf.js
grep DEBUG webpack/webpack.common.js
docker-compose -f src/main/docker/app.yml up -d # spin up production mode application
sleep 120 # wait for app to start up
docker-compose -f src/main/docker/app.yml logs # show output of app startup
yarn e2e # run e2e tests against production mode
docker-compose -f src/main/docker/app.yml down -v # clean up containers and volumes
git checkout src/test/javascript/protractor.conf.js  # revert protractor configuration
git checkout webpack/webpack.common.js
