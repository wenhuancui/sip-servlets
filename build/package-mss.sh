#!/bin/bash

if [[ $# -lt 2 ]]; then
   echo "Usage: $0 [profile to package] [version to package]"
   echo " E.g: $0 all mss-1.6.1-SNAPSHOT"
   exit 1
fi

PROFILE=$1
VERSION=$2

MYHOME=$PWD
echo "Packaging Mobicents Sip Servlets profile $PROFILE"
cd ../sip-servlets/sip-servlets-$PROFILE-beans
mvn package 
cd ../sip-servlets-$PROFILE-deployer
mvn package 

cd ../../
cd ./sip-servlets/sip-servlets-all-beans/target/sip-servlets-all-beans*/jbossweb.sar
tar cvf $MYHOME/$VERSION.tar *jar --exclude="servlet-api*" 
cd ../../../../../
cd ./sip-servlets/sip-servlets-all-deployer/target/sip-servlets-all-deployer*/jbossweb.deployer 
tar rvf $MYHOME/$VERSION.tar sip-servlets-jboss5-deployer*jar  
cd ../../../../../
cd  ./sip-servlets/sip-servlets-jboss5-ha-server-cache/target/
tar rvf $MYHOME/$VERSION.tar sip-servlets-jboss5-ha-server-cache*jar --exclude="*source*" 

echo "Completed building MSS."
