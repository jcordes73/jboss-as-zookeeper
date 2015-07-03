# jboss-as-zookeeper
A ZooKeeper subsystem for JBoss EAP 6

## Install JMX dependencies

- Download Java Management Extension (JMX) 1.2.1 from http://www.oracle.com/technetwork/java/javasebusiness/downloads/java-archive-downloads-java-plat-419418.html
- unzip jmx-1_2_1-ri.zip
- cd jmx-1_2_1-bin/lib
```
mvn org.apache.maven.plugins:maven-install-plugin:2.5.2:install-file -Dfile=jmxtools.jar -DgroupId=com.sun.jdmk -DartifactId=jmxtools -Dversion=1.2.1 -Dpackaging=jar 
mvn org.apache.maven.plugins:maven-install-plugin:2.5.2:install-file -Dfile=jmxri.jar -DgroupId=com.sun.jmx -DartifactId=jmxri -Dversion=1.2.1 -Dpackaging=jar 
```

## Build
To build the project run
```mvn clean install```

## Deploy
Now you are ready to deploy the subsystem to JBoss EAP

```cp -rf target/module/org/ $JBOSS_HOME/modules/system/layers/base
cp $JBOSS_HOME/docs/examples/configs/standalone-minimalistic.xml $JBOSS_HOME/standalone/configuration/
$JBOSS_HOME/bin/standalone.sh -c standalone-minimalistic.xml > nohup.out 2>&1 &```

## Configure
Via the JBoss CLI you can now go on and configure the subsystem

```$JBOSS_HOME/bin/jboss-cli.sh -c
/extension=org.jboss.as.zookeeper:add(module=org.jboss.as.zookeeper)
/socket-binding-group=standard-sockets/socket-binding=zookeeper:add(port=2181)
/subsystem=zookeeper:add
/subsystem=zookeeper/server=default:add```


For SASL authentication I added the following security-domain to the security subsystem:

```xml
<security-domain name="zookeeper" cache-type="default">
  <authentication>
    <login-module code="UsersRoles" flag="required">
      <module-option name="unauthenticatedIdentity" value="super"/>
      <module-option name="usersProperties" value="file://${jboss.server.config.dir}/zookeeper-users.properties"/>
      <module-option name="rolesProperties" value="file://${jboss.server.config.dir}/zookeeper-roles.properties"/>
    </login-module>
  </authentication>
</security-domain>
```

```
java -cp target/dependencies/zookeeper-3.4.5.jar:$JBOSS_HOME/modules/system/layers/base/org/slf4j/main/slf4j-api-1.7.2.redhat-3.jar org.apache.zookeeper.server.auth.DigestAuthenticationProvider user:password
SLF4J: Failed to load class "org.slf4j.impl.StaticLoggerBinder".
SLF4J: Defaulting to no-operation (NOP) logger implementation
SLF4J: See http://www.slf4j.org/codes.html#StaticLoggerBinder for further details.
user:password->user:tpUq/4Pn5A64fVZyQ0gOJ8ZWqkY=
```

See https://cwiki.apache.org/confluence/display/ZOOKEEPER/Zookeeper+and+SASL 

$JBOSS_HOME/standalone/configuration/zookeeper-users.properties 
```
user_super=87ac910e3c04f81fb0449c614bfa6bd6
user_guest=zrwGGHoYa1x7P/8qXdr90ox6SC8=
```

$JBOSS_HOME/standalone/configuration/zookeeper-roles.properties 
```
super=super
```
