#!/bin/bash

#if [[ $# -lt 1 ]]; then
#   echo "Usage: $0 [profile to build]"
#   exit 1
#fi

#PROFILE=$1

echo "Cleaning Mobicents Sip Servlets "
cd ../parent
mvn clean 

cd ../cluster
mvn clean
##diameter-docs
##diameter-mux
cd ../jain-sip
mvn clean 
cd ../jain-sip-170.x/m2
mvn clean 
cd ../../jain-sip-ha
mvn clean 
cd ../jain-sip-ext
mvn clean 

##jainslee
##jbossweb-5-exposed
##jdiameter
##jdiameter-ha
##media
##presence
##protocols
##tools/maven-du-plugin
cd ../tools/maven-eclipse-plugin
mvn clean 
##maven-library-plugin
cd ../../sip-balancer
mvn clean 
cd ../sip-servlets
mvn clean 


echo "Completed cleaning MSS."
