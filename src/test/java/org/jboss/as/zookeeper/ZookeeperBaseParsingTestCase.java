package org.jboss.as.zookeeper;

import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.ADD;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.OP;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.OP_ADDR;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.PORT;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.SOCKET_BINDING;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.SOCKET_BINDING_GROUP;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.Executors;

import org.jboss.as.controller.PathAddress;
import org.jboss.as.controller.PathElement;
import org.jboss.as.controller.RunningMode;
import org.jboss.as.server.Services;
import org.jboss.as.subsystem.test.AbstractSubsystemBaseTest;
import org.jboss.as.subsystem.test.AdditionalInitialization;
import org.jboss.as.subsystem.test.ControllerInitializer;
import org.jboss.dmr.ModelNode;
import org.jboss.msc.service.ServiceController;
import org.jboss.msc.service.ServiceTarget;
import org.jboss.msc.service.ValueService;
import org.jboss.msc.value.ImmediateValue;

/**
 * This is the barebone test example that tests subsystem It does same things
 * that {@link ZookeeperParsingTestCase} does but most of internals are already
 * done in AbstractSubsystemBaseTest If you need more control over what happens
 * in tests look at {@link ZookeeperParsingTestCase}
 *
 * @author <a href="mailto:tomaz.cerar@redhat.com">Tomaz Cerar</a>
 */
public class ZookeeperBaseParsingTestCase extends AbstractSubsystemBaseTest {

    public ZookeeperBaseParsingTestCase() {
        super(ZooKeeperExtension.SUBSYSTEM_NAME, new ZooKeeperExtension());
    }

    @Override
    protected String getSubsystemXml() throws IOException {
        return readResource("subsystem.xml");
    }

    @Override
    protected AdditionalInitialization createAdditionalInitialization() {
        return new AdditionalInitialization() {
            @Override
            protected RunningMode getRunningMode() {
                return RunningMode.NORMAL;
            }

            protected ControllerInitializer createControllerInitializer() {
                ControllerInitializer ci = new ControllerInitializer() {

                    @Override
                    protected void initializeSocketBindingsOperations(List<ModelNode> ops) {

                        super.initializeSocketBindingsOperations(ops);

                        final String[] names = {"http", "zookeeper"};
                        final int[] ports = {8080, 55555};
                        for (int i = 0; i < names.length; i++) {
                            final ModelNode op = new ModelNode();
                            op.get(OP).set(ADD);
                            op.get(OP_ADDR).set(PathAddress.pathAddress(PathElement.pathElement(SOCKET_BINDING_GROUP, SOCKET_BINDING_GROUP_NAME),
                                    PathElement.pathElement(SOCKET_BINDING, names[i])).toModelNode());
                            op.get(PORT).set(ports[i]);
                            ops.add(op);
                        }
                    }
                };

                // Adding a socket-binding is what triggers ControllerInitializer to set up the interface
                // and socket-binding-group stuff we depend on TODO something less hacky
                ci.addSocketBinding("make-framework-happy", 59999);
                return ci;
            }

            @Override
            protected void addExtraServices(ServiceTarget target) {
                super.addExtraServices(target);
                target.addService(Services.JBOSS_SERVER_EXECUTOR, new ValueService<>(new ImmediateValue<>(Executors.newFixedThreadPool(2))))
                        .setInitialMode(ServiceController.Mode.ACTIVE)
                        .install();
            }
        };
    }
}