#!/bin/bash

if [[ $# -lt 1 ]]; then
   echo "Usage: $0 [profile to build]"
   echo " E.g: $0 jboss-5"
   exit 1
fi

PROFILE=$1

if [ "$WORKSPACE" == "" ]; then
 echo "Workspace is not set, using current directory"
 WORKSPACE=$PWD
fi

echo "Building Mobicents Sip Servlets profile $PROFILE"
cd $WORKSPACE/parent
mvn clean install

cd $WORKSPACE/tools/maven-eclipse-plugin
mvn clean install


#cd ../jain-sip
cd $WORKSPACE/jain-sip-170.x/m2
mvn clean install

cd $WORKSPACE/cluster
mvn clean install
cd $WORKSPACE/sip-balancer
mvn clean install

cd $WORKSPACE/jain-sip-ha
mvn clean install
cd $WORKSPACE/jain-sip-ext
mvn clean install

cd $WORKSPACE/sip-servlets
mvn clean install -P$PROFILE
##tools/maven-du-plugin

##maven-library-plugin

##diameter-docs
##diameter-mux


##jainslee
##jbossweb-5-exposed
##jdiameter
##jdiameter-ha
##media
##presence
##protocols


cd $WORKSPACE/sip-servlets/sip-servlets-all-beans
mvn package
cd $WORKSPACE/sip-servlets/sip-servlets-all-deployer
mvn package


echo "Completed building MSS."
