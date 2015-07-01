# jboss-as-zookeeper
A zookeeper subsystem for JBoss EAP 6

JMX

- Download Java Management Extension (JMX) 1.2.1 from http://www.oracle.com/technetwork/java/javasebusiness/downloads/java-archive-downloads-java-plat-419418.html

- unzip jmx-1_2_1-ri.zip
- Install into local maven repo
  - cd jmx-1_2_1-bin/lib
  - mvn install

mvn org.apache.maven.plugins:maven-install-plugin:2.5.2:install-file -Dfile=jmxtools.jar -DgroupId=com.sun.jmx -DartifactId=jmxtools -Dversion=1.2.1 -Dpackaging=jar 

mvn org.apache.maven.plugins:maven-install-plugin:2.5.2:install-file -Dfile=jmxri.jar -DgroupId=com.sun.jmx -DartifactId=jmxri -Dversion=1.2.1 -Dpackaging=jar 

mvn clean install

cp -rf target/module/org/ $JBOSS_HOME/modules/system/layers/base

cp $JBOSS_HOME/docs/examples/configs/standalone-minimalistic.xml $JBOSS_HOME/standalone/configuration/

$JBOSS_HOME/bin/standalone.sh -c standalone-minimalistic.xml > nohup.out 2>&1 &

$JBOSS_HOME/bin/jboss-cli.sh -c
/extension=org.jboss.as.zookeeper:add(module=org.jboss.as.zookeeper)
/socket-binding-group=standard-sockets/socket-binding=zookeeper:add(port=2181)
/subsystem=zookeeper:add
/subsystem=zookeeper/server=default:add
