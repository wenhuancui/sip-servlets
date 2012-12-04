#!/bin/bash

if [[ $# -lt 1 ]]; then
    echo "Usage: $0 [version number]"
    echo " E.g: $0 mss-1.6.1-SNAPSHOT"
    exit 1
fi

NEW_VERSION=$1

echo "Updating the version to $NEW_VERSION"
cd ../parent
mvn versions:set -DnewVersion=$NEW_VERSION
cd ../cluster
mvn versions:set -DnewVersion=$NEW_VERSION
##diameter-docs
##diameter-mux
##cd ../jain-sip
##mvn versions:set -DnewVersion=$NEW_VERSION
cd ../jain-sip-ext
mvn versions:set -DnewVersion=$NEW_VERSION
cd ../jain-sip-ha
mvn versions:set -DnewVersion=$NEW_VERSION
##jainslee
##jbossweb-5-exposed
##jdiameter
##jdiameter-ha
##media
##presence
##protocols
cd ../sip-balancer
mvn versions:set -DnewVersion=$NEW_VERSION
cd ../sip-servlets
mvn versions:set -DnewVersion=$NEW_VERSION
##tools/maven-du-plugin
cd ../tools/maven-eclipse-plugin
mvn versions:set -DnewVersion=$NEW_VERSION
##maven-library-plugin

echo "Completed version update."
