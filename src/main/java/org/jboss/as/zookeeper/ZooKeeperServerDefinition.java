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

import java.util.Arrays;
import java.util.List;

import org.jboss.as.controller.ReloadRequiredWriteAttributeHandler;
import org.jboss.as.controller.SimpleAttributeDefinition;
import org.jboss.as.controller.SimpleAttributeDefinitionBuilder;
import org.jboss.as.controller.SimpleResourceDefinition;
import org.jboss.as.controller.registry.AttributeAccess;
import org.jboss.as.controller.registry.ManagementResourceRegistration;
import org.jboss.dmr.ModelNode;
import org.jboss.dmr.ModelType;
import org.jboss.dmr.ValueExpression;

/**
 * @author <a href="jcordes@redhat.com">Jochen Cordes</a>
 */
public class ZooKeeperServerDefinition extends SimpleResourceDefinition {
	
	public static final ZooKeeperServerDefinition INSTANCE = new ZooKeeperServerDefinition();
	
	
	public static final SimpleAttributeDefinition SOCKET_BINDING = new SimpleAttributeDefinitionBuilder(
            ZooKeeperExtension.SOCKET_BINDING, ModelType.STRING).setAllowExpression(true)
	        .setXmlName(ZooKeeperExtension.SOCKET_BINDING)
	        .setFlags(AttributeAccess.Flag.RESTART_RESOURCE_SERVICES)
            .setDefaultValue(new ModelNode().set(ZooKeeperExtension.BINDING_NAME)).setAllowNull(true).build();

	// we define attribute named tick
	protected static final SimpleAttributeDefinition TICK_TIME = new SimpleAttributeDefinitionBuilder(
			ZooKeeperExtension.TICK_TIME, ModelType.LONG).setAllowExpression(true)
			.setXmlName(ZooKeeperExtension.TICK_TIME)
			.setFlags(AttributeAccess.Flag.RESTART_RESOURCE_SERVICES)
			.setDefaultValue(new ModelNode(2000)).setAllowNull(true).build();

	// we define attribute named dataDir
	protected static final SimpleAttributeDefinition DATA_DIR = new SimpleAttributeDefinitionBuilder(
			ZooKeeperExtension.DATA_DIR, ModelType.STRING)
			.setAllowExpression(true).setXmlName(ZooKeeperExtension.DATA_DIR)
			.setFlags(AttributeAccess.Flag.RESTART_RESOURCE_SERVICES)
			.setDefaultValue(new ModelNode(new ValueExpression("${jboss.server.data.dir}/zookeeper")))
			.setAllowNull(true).build();
	
	static final List<SimpleAttributeDefinition> ZOOKEEPER_ATTRIBUTES = Arrays.asList(SOCKET_BINDING, TICK_TIME, DATA_DIR);

	private ZooKeeperServerDefinition() {
		super(ZooKeeperExtension.SERVER_PATH, ZooKeeperExtension
				.getResourceDescriptionResolver());
	}

	@Override
	public void registerOperations(
			ManagementResourceRegistration resourceRegistration) {
		resourceRegistration.registerOperationHandler(ZooKeeperServerAdd.DEFINITION, ZooKeeperServerAdd.INSTANCE);
		resourceRegistration.registerOperationHandler(ZooKeeperServerRemove.DEFINITION, ZooKeeperServerRemove.INSTANCE);
	}

	@Override
	public void registerAttributes(
			ManagementResourceRegistration resourceRegistration) {
		for (SimpleAttributeDefinition attr : ZooKeeperServerDefinition.ZOOKEEPER_ATTRIBUTES) {
            resourceRegistration.registerReadWriteAttribute(attr, null, new ReloadRequiredWriteAttributeHandler(attr));
        }
	}
}
