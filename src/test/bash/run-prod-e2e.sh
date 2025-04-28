#!/bin/bash

# For testing production mode e2e tests we start up a docker postgres server since production mode is configured for a
# postgres database instead of in-memory database.

set -e
echo "Running production e2e tests"
cp src/test/resources/config/keystore.p12 src/main/docker/etc/config
cp src/test/resources/config/keystore.p12 src/main/resources/config

./gradlew bootrun -Pe2e-prod-test -x test -x javadocJar &
docker compose -f src/main/docker/postgresql.yml up -d # spin up production mode application
set +e

if ./gradlew generateOpenApiSpec && yarn run e2e-prod; then
  EXIT_STATUS=0
else
  EXIT_STATUS=1
fi

docker compose -f src/main/docker/postgresql.yml down -v # clean up containers and volumes
exit $EXIT_STATUS
