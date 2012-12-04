#!/bin/bash

if [ "$WORKSPACE" == "" ]; then
 echo "Workspace is not set, using current directory"
 WORKSPACE=$PWD
fi

echo "Cleaning Mobicents Sip Servlets"
cd $WORKSPACE/parent
mvn clean 

cd $WORKSPACE/tools/maven-eclipse-plugin
mvn clean 


#cd ../jain-sip
cd $WORKSPACE/jain-sip-170.x/m2
mvn clean 

cd $WORKSPACE/cluster
mvn clean 
cd $WORKSPACE/sip-balancer
mvn clean 

cd $WORKSPACE/jain-sip-ha
mvn clean 
cd $WORKSPACE/jain-sip-ext
mvn clean 

cd $WORKSPACE/sip-servlets
mvn clean 
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
mvn clean 
cd $WORKSPACE/sip-servlets/sip-servlets-all-deployer
mvn clean 


echo "Completed cleaning MSS."
