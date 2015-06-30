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

import java.util.List;

import org.apache.zookeeper.server.ZooKeeperServer;
import org.jboss.as.controller.AbstractAddStepHandler;
import org.jboss.as.controller.OperationContext;
import org.jboss.as.controller.OperationDefinition;
import org.jboss.as.controller.OperationFailedException;
import org.jboss.as.controller.ServiceVerificationHandler;
import org.jboss.as.controller.SimpleAttributeDefinition;
import org.jboss.as.controller.SimpleOperationDefinitionBuilder;
import org.jboss.as.controller.registry.OperationEntry;
import org.jboss.as.network.SocketBinding;
import org.jboss.as.server.Services;
import org.jboss.dmr.ModelNode;
import org.jboss.dmr.ModelType;
import org.jboss.logging.Logger;
import org.jboss.msc.service.ServiceBuilder;
import org.jboss.msc.service.ServiceBuilder.DependencyType;
import org.jboss.msc.service.ServiceController;
import org.jboss.msc.service.ServiceController.Mode;

/**
 * Handler responsible for adding the subsystem resource to the model
 *
 * @author <a href="jcordes@redhat.com">Jochen Cordes</a>
 */
class ZooKeeperServerAdd extends AbstractAddStepHandler {

	static final OperationDefinition DEFINITION = new SimpleOperationDefinitionBuilder(
			"add", ZooKeeperExtension.getResourceDescriptionResolver())
			.setReplyType(ModelType.BOOLEAN)
			.withFlag(OperationEntry.Flag.RESTART_NONE)
			.addParameter(ZooKeeperServerDefinition.TICK_TIME)
			.addParameter(ZooKeeperServerDefinition.DATA_DIR)
			.addParameter(ZooKeeperServerDefinition.SOCKET_BINDING).build();

	static final ZooKeeperServerAdd INSTANCE = new ZooKeeperServerAdd();

	private final Logger log = Logger.getLogger(ZooKeeperServerAdd.class);

	private ZooKeeperServerAdd() {
	}

	/** {@inheritDoc} */
	@Override
	protected void populateModel(ModelNode operation, ModelNode model)
			throws OperationFailedException {
		log.info("Populating the model");
		ZooKeeperServerDefinition.SOCKET_BINDING.validateAndSet(operation,
				model);
		ZooKeeperServerDefinition.DATA_DIR.validateAndSet(operation, model);
		ZooKeeperServerDefinition.TICK_TIME.validateAndSet(operation, model);
	}

	/** {@inheritDoc} */
	@Override
	public void performRuntime(OperationContext context, ModelNode operation,
			final ModelNode model,
			final ServiceVerificationHandler verificationHandler,
			final List<ServiceController<?>> newControllers)
			throws OperationFailedException {

		log.info("performRuntime start");

		for (final SimpleAttributeDefinition attribute : ZooKeeperServerDefinition.ZOOKEEPER_ATTRIBUTES) {
			if (operation.get(attribute.getName()).isDefined()) {
				attribute.validateAndSet(operation, model);
			} else {
				ModelNode modelAttrNode = model.get(attribute.getName());
				ModelNode defaultValue = attribute.getDefaultValue();
				if (defaultValue != null) {
					modelAttrNode.set(defaultValue);
				}
			}
		}

		final long tick = ZooKeeperServerDefinition.TICK_TIME
				.resolveModelAttribute(context, model).asLong();
		final String dataDir = ZooKeeperServerDefinition.DATA_DIR
				.resolveModelAttribute(context, model).resolve().asString();

		ZooKeeperService service = new ZooKeeperService(tick, dataDir);

		final ServiceBuilder<ZooKeeperServer> builder = context
				.getServiceTarget()
				.addService(ZooKeeperService.SERVICE_NAME, service)
				.addDependency(
						DependencyType.REQUIRED,
						SocketBinding.JBOSS_BINDING_NAME
								.append(ZooKeeperExtension.BINDING_NAME),
						SocketBinding.class, service.getBinding());
		builder.setInitialMode(Mode.ACTIVE);

		Services.addServerExecutorDependency(builder, service.getExecutor(),
				false);

    	builder.addListener(verificationHandler);

		newControllers.add(builder.install());

		log.info("performRuntime end");
	}
}
