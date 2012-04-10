Building JBCP on your local machine using EAP unsigned release
--------------------------------------------------------------------
Assumption 0: You made a svn checkout of http://mobicents.googlecode.com/svn/branches/jbcp/5.1.1_HF/ branch
Assumption 1: You have your EAP version installed locally and set $JBOSS_HOME variable
Assumption 2: Your EAP version is unsigned for local DEV purpose. You may achive this by using command
find $JBOSS_HOME -name '*.jar' -print0 | xargs -0 -I JAR zip -d JAR META-INF/JBOSSCOD.*" 
for UNIX based OS.

1. cd into build folder
2. type: ant -f build-MSS.xml -Djboss.node=${jboss configuration} -Djboss.home=${jboss home} -Dmvn.build.profile=${maven profile for MSS}
- when you skip -Djboss.home=${jboss home} it will use env variable $JBOSS_HOME
- when you skip -Djboss.node=${jboss configuration} it will use "default" as configuration node
- when you skip -Dmvn.build.profile=${maven profile for MSS} it will use "jboss-5" as maven profile for MSS to build
3.There is a MSS deployed in the configuration node you selected in step 2.   

Building JBCP on your local machine and repackage JBCP release
--------------------------------------------------------------------
Assumption 0: You made a svn checkout of http://mobicents.googlecode.com/svn/branches/jbcp/5.1.1_HF/ branch
Assumption 1: You have your JBCP version downloaded locally 
Assumption 2: Your JBCP version is unsigned. You may achive this by using command
find [JBCP folder] -name '*.jar' -print0 | xargs -0 -I JAR zip -d JAR META-INF/JBOSSCOD.*" 
for UNIX based OS.

1. cd into build folder
2. type: ant package-JBCP -f build-MSS.xml -Djbcp.distro.zip.path=[Path to JBCP distro]
Example:
ant package-JBCP -f build-MSS.xml -Djbcp.distro.zip.path=/home/pslegr/jbcp-development/jbcp2/release-CP/MP_5.1.3_CR01/full-dist/JBCP-5.1.3.CR01.zip
3.There is created a repackaged JBCP zip bundle in build folder.   
