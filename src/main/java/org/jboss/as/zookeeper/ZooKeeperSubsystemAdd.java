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

import org.jboss.as.controller.AbstractBoottimeAddStepHandler;
import org.jboss.as.controller.OperationContext;
import org.jboss.as.controller.OperationDefinition;
import org.jboss.as.controller.OperationFailedException;
import org.jboss.as.controller.ServiceVerificationHandler;
import org.jboss.as.controller.SimpleOperationDefinitionBuilder;
import org.jboss.as.controller.registry.OperationEntry;
import org.jboss.dmr.ModelNode;
import org.jboss.dmr.ModelType;
import org.jboss.logging.Logger;
import org.jboss.msc.service.ServiceController;

/**
 * Handler responsible for adding the subsystem resource to the model
 *
 * @author <a href="jcordes@redhat.com">Jochen Cordes</a>
 */
class ZooKeeperSubsystemAdd extends AbstractBoottimeAddStepHandler {
	
	static final OperationDefinition DEFINITION = new SimpleOperationDefinitionBuilder("add",
            ZooKeeperExtension.getResourceDescriptionResolver())
            .setReplyType(ModelType.BOOLEAN)
            .withFlag(OperationEntry.Flag.RESTART_RESOURCE_SERVICES)
            .build();
	
	static final ZooKeeperSubsystemAdd INSTANCE = new ZooKeeperSubsystemAdd();

	private final Logger log = Logger.getLogger(ZooKeeperSubsystemAdd.class);

	private ZooKeeperSubsystemAdd() {
	}

	/** {@inheritDoc} */
	@Override
	protected void populateModel(ModelNode operation, ModelNode model)
			throws OperationFailedException {
		log.info("Populating the model");
		model.setEmptyObject();
	}

	/** {@inheritDoc} */
	@Override
	public void performBoottime(OperationContext context, ModelNode operation,
			final ModelNode model,
			final ServiceVerificationHandler verificationHandler,
			final List<ServiceController<?>> newControllers)
			throws OperationFailedException {
		log.info("performBoottime start");
		
		log.info("performBoottime end");
	}
	
}
