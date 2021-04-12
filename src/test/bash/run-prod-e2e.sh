#!/bin/bash

# For testing production mode e2e tests we need some tweaks:
# 1) The angular app needs to run in dev mode for protractor to work
# 2) We update the basehref of the angular app to the context path of the backend
# 3) We update the protractor configuration to the new path
#
# Then we start up a docker stack with a postgres server since production mode is configured for a
# postgres database instead of in-memory database.

set -e

# only run on the release branch and master branch if it's not a tag build
echo "Running production e2e tests"
yarn run webpack:prod
sed -i "s|new plugin.BaseHrefWebpackPlugin({ baseHref: '/' })|new plugin.BaseHrefWebpackPlugin({ baseHref: '/managementportal/' })|" webpack/webpack.dev.js
sed -i "s|baseUrl: 'http://localhost:8080/',|baseUrl: 'http://localhost:8080/managementportal/',|" src/test/javascript/protractor.conf.js
sed -i "s|contexts: prod|contexts: dev|" src/main/resources/config/application-prod.yml # set liquibase context to dev so it loads demo data
cp src/test/resources/config/keystore.p12 src/main/docker/etc/config
./gradlew bootRepackage -Pprod buildDocker -x test

docker-compose -f src/main/docker/app.yml up -d # spin up production mode application
set +e

# wait for app to be up
yarn run wait-for-managementportal
# run e2e tests against production mode
if ./gradlew generateOpenApiSpec && yarn e2e; then
  EXIT_STATUS=0
else
  EXIT_STATUS=1
fi
docker-compose -f src/main/docker/app.yml logs # show output of app startup
docker-compose -f src/main/docker/app.yml down -v # clean up containers and volumes

exit $EXIT_STATUS
