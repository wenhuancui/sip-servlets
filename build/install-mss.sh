#!/bin/bash

if [[ $# -lt 1 ]]; then
   echo "Usage: $0 [profile to build]"
   echo " E.g: $0 jboss-5"
   exit 1
fi

PROFILE=$1

echo "Building Mobicents Sip Servlets profile $PROFILE"
cd ../parent
mvn clean install

#cd ../jain-sip
cd ../jain-sip-170.x/m2
mvn clean install
cd ../../jain-sip-ha
mvn clean install
cd ../jain-sip-ext
mvn clean install

cd ../sip-servlets
mvn clean install -P$PROFILE
##tools/maven-du-plugin
cd ../tools/maven-eclipse-plugin
mvn clean install
##maven-library-plugin
cd ../../cluster
mvn clean install
##diameter-docs
##diameter-mux


##jainslee
##jbossweb-5-exposed
##jdiameter
##jdiameter-ha
##media
##presence
##protocols

cd ../sip-balancer
mvn clean install



echo "Completed building MSS."
