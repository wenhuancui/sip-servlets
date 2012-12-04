#!/bin/bash

##   echo "Usage: $0 [version to package]"
##   echo " E.g: $0  $VERSION"

if [[ $# -lt 1 ]]; then
  echo "No args passed, so using default version"
  VERSION=mss-1.6.1-RC03 
else 
  VERSION=$1
fi



if [ "$WORKSPACE" == "" ]; then
  echo "Workspace variable is not set, using current directory"
  WORKSPACE=$PWD
fi

echo "Packaging Mobicents Sip Servlets version $VERSION"
cd $WORKSPACE/sip-servlets/sip-servlets-all-beans/target/sip-servlets-all-beans-$VERSION-distribution/jbossweb.sar;
tar -cvf $WORKSPACE/$VERSION.tar *.jar --exclude="servlet-api*";
cd $WORKSPACE/sip-servlets/sip-servlets-all-deployer/target/sip-servlets-all-deployer-$VERSION-distribution/jbossweb.deployer;
tar -rvf $WORKSPACE/$VERSION.tar sip-servlets-jboss5-deployer*.jar;
cd $WORKSPACE/sip-servlets/sip-servlets-jboss5-ha-server-cache/target/;
tar -rvf $WORKSPACE/$VERSION.tar sip-servlets-jboss5-ha-server-cache*jar --exclude="*source*";
tar -tvf $WORKSPACE/$VERSION.tar;
echo "Completed building MSS."

echo "Packaging Mobicents Sip Servlets sources"
mkdir $WORKSPACE/mss-sources-tmp;
find $WORKSPACE/.repository -name "*sources.jar" | xargs -I {} cp {} $WORKSPACE/mss-sources-tmp;
tar -C $WORKSPACE/mss-sources-tmp -cvf $WORKSPACE/$VERSION-sources.tar .
tar -tvf $WORKSPACE/$VERSION-sources.tar;
rm -rf $WORKSPACE/mss-sources-tmp;
echo "Completed building sources for MSS."
