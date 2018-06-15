#!/bin/bash

echo -e "Building javadoc...\n"

./gradlew javadoc

echo -e "Publishing javadoc...\n"

cp build/swagger-spec/swagger.json $HOME/swagger.json
cp -R build/docs/javadoc $HOME/mp-javadoc
cp -R oauth-client-util/build/docs/javadoc $HOME/ocu-javadoc
cp -R radar-auth/build/docs/javadoc $HOME/ra-javadoc
cp -R managementportal-client/build/docs/javadoc $HOME/mpc-javadoc

cd $HOME
git config --global user.email "travis@travis-ci.org"
git config --global user.name "travis-ci"
git clone --quiet --branch=gh-pages https://${GH_TOKEN}@github.com/RADAR-base/ManagementPortal.git gh-pages > /dev/null

cd gh-pages
git rm -rf ./*-javadoc
git rm -f ./apidoc/swagger.json
cp -Rf $HOME/*-javadoc .
cp $HOME/swagger.json apidoc/swagger.json
git add -f .
git commit -m "Latest javadoc on successful travis build $TRAVIS_BUILD_NUMBER auto-pushed to gh-pages"
git push -fq origin gh-pages > /dev/null

echo -e "Published Javadoc to gh-pages.\n"
