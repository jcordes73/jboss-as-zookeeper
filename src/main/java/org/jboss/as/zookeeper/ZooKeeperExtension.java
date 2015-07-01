/*
* JBoss, Home of Professional Open Source.
* Copyright 2010, Red Hat, Inc., and individual contributors
* as indicated by the @author tags. See the copyright.txt file in the
* distribution for a full listing of individual contributors.
*
* This is free software; you can redistribute it and/or modify it
* under the terms of the GNU Lesser General Public License as
* published by the Free Software Foundation; either version 2.1 of
* the License, or (at your option) any later version.
*
* This software is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
* Lesser General Public License for more details.
*
* You should have received a copy of the GNU Lesser General Public
* License along with this software; if not, write to the Free
* Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
* 02110-1301 USA, or see the FSF site: http://www.fsf.org.
*/

package org.jboss.as.zookeeper;

import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.SUBSYSTEM;

import org.jboss.as.controller.Extension;
import org.jboss.as.controller.ExtensionContext;
import org.jboss.as.controller.PathElement;
import org.jboss.as.controller.SubsystemRegistration;
import org.jboss.as.controller.descriptions.StandardResourceDescriptionResolver;
import org.jboss.as.controller.operations.common.GenericSubsystemDescribeHandler;
import org.jboss.as.controller.parsing.ExtensionParsingContext;
import org.jboss.as.controller.registry.ManagementResourceRegistration;

/**
 * @author <a href="jcordes@redhat.com">Jochen Cordes</a>
 */
public class ZooKeeperExtension implements Extension {

    /**
     * The name space used for the {@code subsystem} element
     */
    public static final String NAMESPACE = "urn:jboss:domain:zookeeper:1.0";

    /**
     * The name of our subsystem within the model.
     */
    public static final String SUBSYSTEM_NAME = "zookeeper";
    public static final String SERVER = "server";

    public static final String SOCKET_BINDING = "socket-binding";
    public static final String BINDING_NAME = "zookeeper";
    public static final String DATA_DIR = "dataDir";
    public static final String TICK_TIME = "tickTime";

    /**
     * The parser used for parsing our subsystem
     */
    private final ZooKeeperParser parser = new ZooKeeperParser();

    protected static final PathElement SUBSYSTEM_PATH = PathElement.pathElement(SUBSYSTEM, SUBSYSTEM_NAME);
    protected static final PathElement SERVER_PATH = PathElement.pathElement(SERVER);

    private static final String RESOURCE_NAME = ZooKeeperExtension.class
            .getPackage().getName() + ".LocalDescriptions";

    static StandardResourceDescriptionResolver getResourceDescriptionResolver(
            final String... keyPrefix) {
        StringBuilder prefix = new StringBuilder(SUBSYSTEM_NAME);
        for (String kp : keyPrefix) {
            prefix.append('.').append(kp);
        }

        return new StandardResourceDescriptionResolver(prefix.toString(),
                RESOURCE_NAME, ZooKeeperExtension.class.getClassLoader(), true,
                false);
    }

    @Override
    public void initializeParsers(ExtensionParsingContext context) {
        context.setSubsystemXmlMapping(SUBSYSTEM_NAME, NAMESPACE, parser);
    }

    @Override
    public void initialize(ExtensionContext context) {
        final SubsystemRegistration subsystem = context.registerSubsystem(SUBSYSTEM_NAME, 1, 0);
        ManagementResourceRegistration registration = subsystem.registerSubsystemModel(ZooKeeperSubsystemDefinition.INSTANCE);
        registration.registerSubModel(ZooKeeperServerDefinition.INSTANCE);
        registration.registerOperationHandler(GenericSubsystemDescribeHandler.DEFINITION, GenericSubsystemDescribeHandler.INSTANCE);
        subsystem.registerXMLElementWriter(parser);
    }
}
