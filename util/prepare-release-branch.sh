#!/bin/bash

if [ -z $1 ]
then
  echo "Usage: $0 <version>"
  echo ""
  echo "This script will create a release branch and update the version numbers in the files where"
  echo "necessary. It is then up to you to double check the work this script has done and commit"
  echo "the changes."
  echo ""
  echo "Example usage: $0 1.5.2"
  exit 0
fi

VERSION=$1

git branch release-$VERSION
git checkout release-$VERSION

sed -i 's#  \"version\": \".*\",#  "version": "'"$VERSION"'",#' package.json
sed -i 's#radarcns/management-portal:.*#radarbase/management-portal:'"$VERSION"'#' src/main/docker/management-portal.yml
sed -i 's#radarcns/management-portal:.*#radarbase/management-portal:'"$VERSION"'#' README.md
sed -i "s#compile group: 'org.radarcns', name: 'oauth-client-util', version: '.*'#compile group: 'org.radarcns', name: 'oauth-client-util', version: '$VERSION'#" oauth-client-util/README.md
sed -i "s#compile group: 'org.radarcns', name: 'radar-auth', version: '.*'#compile group: 'org.radarcns', name: 'radar-auth', version: '$VERSION'#" radar-auth/README.md
sed -i "s#version '.*' // project version#version '$VERSION' // project version#" build.gradle
