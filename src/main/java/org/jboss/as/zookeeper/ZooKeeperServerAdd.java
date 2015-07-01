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
import org.jboss.as.controller.OperationFailedException;
import org.jboss.as.controller.OperationStepHandler;
import org.jboss.as.controller.ServiceVerificationHandler;
import org.jboss.as.network.SocketBinding;
import org.jboss.as.server.Services;
import org.jboss.dmr.ModelNode;
import org.jboss.logging.Logger;
import org.jboss.msc.service.ServiceBuilder;
import org.jboss.msc.service.ServiceBuilder.DependencyType;
import org.jboss.msc.service.ServiceController;

/**
 * Handler responsible for adding the subsystem resource to the model
 *
 * @author <a href="jcordes@redhat.com">Jochen Cordes</a>
 */
class ZooKeeperServerAdd extends AbstractAddStepHandler {


    static final ZooKeeperServerAdd INSTANCE = new ZooKeeperServerAdd();

    private final Logger log = Logger.getLogger(ZooKeeperServerAdd.class);

    private ZooKeeperServerAdd() {
        super(ZooKeeperServerDefinition.ZOOKEEPER_ATTRIBUTES);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void performRuntime(OperationContext context, ModelNode operation,final ModelNode model, final ServiceVerificationHandler verificationHandler,
                               final List<ServiceController<?>> newControllers)
            throws OperationFailedException {

        log.info("performRuntime start");

        // This needs to run after all child resources so that they can detect a
        // fresh state
        context.addStep(new OperationStepHandler() {
            @Override
            public void execute(OperationContext context, ModelNode operation)
                    throws OperationFailedException {
                launchServices(context, model, verificationHandler, newControllers);
                context.completeStep(OperationContext.ResultHandler.NOOP_RESULT_HANDLER);
            }
        }, OperationContext.Stage.RUNTIME);

        log.info("performRuntime end");
    }

    protected void launchServices(final OperationContext context,
                                  final ModelNode model,
                                  final ServiceVerificationHandler verificationHandler,
                                  final List<ServiceController<?>> newControllers)
            throws OperationFailedException {
        final long tick = ZooKeeperServerDefinition.TICK_TIME.resolveModelAttribute(context, model).asLong();
        final String dataDir = ZooKeeperServerDefinition.DATA_DIR.resolveModelAttribute(context, model).asString();
        final String socketBinding = ZooKeeperServerDefinition.SOCKET_BINDING.resolveModelAttribute(context, model).asString();

        ZooKeeperService service = new ZooKeeperService(tick, dataDir);

        final ServiceBuilder<ZooKeeperServer> builder = context.getServiceTarget()
                .addService(ZooKeeperService.SERVICE_NAME,service);

        if (verificationHandler != null) {
            builder.addListener(verificationHandler);
        }

        builder.addDependency(SocketBinding.JBOSS_BINDING_NAME.append(socketBinding),SocketBinding.class, service.getBinding());

        Services.addServerExecutorDependency(builder, service.getExecutor(), false);

        ServiceController<?> controller = builder.install();
        if (newControllers != null) {
            newControllers.add(controller);
        }
    }
}
