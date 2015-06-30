package org.jboss.as.zookeeper;

import junit.framework.Assert;

import org.jboss.as.controller.PathAddress;
import org.jboss.as.controller.PathElement;
import org.jboss.as.subsystem.test.AbstractSubsystemTest;
import org.jboss.as.subsystem.test.AdditionalInitialization;
import org.jboss.as.subsystem.test.KernelServices;
import org.jboss.as.subsystem.test.KernelServicesBuilder;
import org.jboss.as.zookeeper.ZooKeeperServerDefinition;
import org.jboss.as.zookeeper.ZooKeeperExtension;
import org.jboss.dmr.ModelNode;
import org.jboss.dmr.Property;
import org.junit.Test;

import java.io.IOException;
import java.util.List;

import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.ADD;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.DESCRIBE;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.OP;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.OP_ADDR;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.SUBSYSTEM;

/**
 * Tests all management expects for subsystem, parsing, marshaling, model
 * definition and other Here is an example that allows you a fine grained
 * controler over what is tested and how. So it can give you ideas what can be
 * done and tested. If you have no need for advanced testing of subsystem you
 * look at {@link ZookeeperBaseParsingTestCase} that testes same stuff but most
 * of the code is hidden inside of test harness
 *
 * @author <a href="kabir.khan@jboss.com">Kabir Khan</a>
 */
public class ZookeeperParsingTestCase extends AbstractSubsystemTest {

	public ZookeeperParsingTestCase() {
		super(ZooKeeperExtension.SUBSYSTEM_NAME, new ZooKeeperExtension());
	}

	/**
	 * Tests that the xml is parsed into the correct operations
	 */
	@Test
	public void testParseSubsystem() throws Exception {		
		// Parse the subsystem xml into operations
		List<ModelNode> operations = super.parse(getSubsystemXml());

		// Check that we have the expected number of operations
		Assert.assertEquals(2, operations.size());

		// Check that each operation has the correct content
		ModelNode addSubsystem = operations.get(0);
		Assert.assertEquals(ADD, addSubsystem.get(OP).asString());
		PathAddress addr = PathAddress.pathAddress(addSubsystem.get(OP_ADDR));
		Assert.assertEquals(1, addr.size());
		PathElement element = addr.getElement(0);
		Assert.assertEquals(SUBSYSTEM, element.getKey());
		Assert.assertEquals(ZooKeeperExtension.SUBSYSTEM_NAME,
				element.getValue());
	}

	/**
	 * Test that the model created from the xml looks as expected
	 */
	@Test
	public void testInstallIntoController() throws Exception {
		KernelServicesBuilder builder = createKernelServicesBuilder(AdditionalInitialization.MANAGEMENT);

		// Parse the subsystem xml and install into the controller
		String subsystemXml =  "subsystem.xml";

		builder.setSubsystemXmlResource(subsystemXml);
		
		KernelServices services = builder.build();

		// Read the whole model and make sure it looks as expected
		ModelNode model = services.readWholeModel();
		Assert.assertTrue(model.get(SUBSYSTEM).hasDefined(
				ZooKeeperExtension.SUBSYSTEM_NAME));
	}

	/**
	 * Test that the model created from the xml looks as expected when no attributes are passed
	 */
	@Test
	public void testInstallIntoControllerNoAttributes() throws Exception {
		KernelServicesBuilder builder = createKernelServicesBuilder(AdditionalInitialization.MANAGEMENT);

		// Parse the subsystem xml and install into the controller
		String subsystemXml =  "<subsystem xmlns=\"urn:jboss:domain:zookeeper:1.0\"><server name=\"test\"/></subsystem>";

		builder.setSubsystemXml(subsystemXml);
		
		KernelServices services = builder.build();

		// Read the whole model and make sure it looks as expected
		ModelNode model = services.readWholeModel();
		ModelNode subsystem = model.get(SUBSYSTEM);
				
		Assert.assertTrue(subsystem.hasDefined(
				ZooKeeperExtension.SUBSYSTEM_NAME));
	}

	/**
	 * Test that the model created from the xml looks as expected when no attributes are passed
	 */
	@Test
	public void testInstallIntoControllerTickTime() throws Exception {
		KernelServicesBuilder builder = createKernelServicesBuilder(AdditionalInitialization.MANAGEMENT);

		// Parse the subsystem xml and install into the controller
		String subsystemXml =  "<subsystem xmlns=\"urn:jboss:domain:zookeeper:1.0\"><server name=\"test\" tickTime=\"3000\"/></subsystem>";

		builder.setSubsystemXml(subsystemXml);
		
		KernelServices services = builder.build();

		// Read the whole model and make sure it looks as expected
		ModelNode model = services.readWholeModel();
		ModelNode subsystem = model.get(SUBSYSTEM);
				
		Assert.assertTrue(subsystem.hasDefined(
				ZooKeeperExtension.SUBSYSTEM_NAME));
		
		ModelNode zookeeper = subsystem.get(ZooKeeperExtension.SUBSYSTEM_NAME);
		
		for (Property property : zookeeper.asPropertyList()) {
			ModelNode entry = property.getValue();
			
			if (ZooKeeperServerDefinition.DATA_DIR.getName().equals(property.getName())) {
				Assert.assertEquals(ZooKeeperServerDefinition.DATA_DIR.getDefaultValue().asString(), entry.asString());
			} else if (ZooKeeperServerDefinition.TICK_TIME.getName().equals(property.getName())) {
				Assert.assertEquals("3000", entry.asString());
			} else if (ZooKeeperServerDefinition.SOCKET_BINDING.getName().equals(property.getName())) {
				Assert.assertEquals(ZooKeeperServerDefinition.SOCKET_BINDING.getDefaultValue().asString(), entry.asString());
			}
		}
	}
	
	/**
	 * Starts a controller with a given subsystem xml and then checks that a
	 * second controller started with the xml marshalled from the first one
	 * results in the same model
	 */
	@Test
	public void testParseAndMarshalModel() throws Exception {
		KernelServicesBuilder builderA = createKernelServicesBuilder(AdditionalInitialization.MANAGEMENT);
		
		// Parse the subsystem xml and install into the first controller
		String subsystemXml =  "subsystem.xml";
		
		builderA.setSubsystemXmlResource(subsystemXml);
		KernelServices servicesA = builderA.build();

		// Get the model and the persisted xml from the first controller
		ModelNode modelA = servicesA.readWholeModel();
		String marshalled = servicesA.getPersistedSubsystemXml();

		System.out.println("Marshalled: "+ marshalled);
		
		// Install the persisted xml from the first controller into a second
		// controller
		KernelServicesBuilder builderB = createKernelServicesBuilder(AdditionalInitialization.MANAGEMENT);
		builderB.setSubsystemXml(marshalled);
		KernelServices servicesB = builderB.build();
		ModelNode modelB = servicesB.readWholeModel();

		// Make sure the models from the two controllers are identical
		super.compare(modelA, modelB);
	}

	/**
	 * Starts a controller with the given subsystem xml and then checks that a
	 * second controller started with the operations from its describe action
	 * results in the same model
	 */
	@Test
	public void testDescribeHandler() throws Exception {
		KernelServicesBuilder builderA = createKernelServicesBuilder(AdditionalInitialization.MANAGEMENT);
		
		// Parse the subsystem xml and install into the first controller
		String subsystemXml =  "subsystem.xml";
		
		builderA.setSubsystemXmlResource(subsystemXml);
		KernelServices servicesA = builderA.build();
		
		// Get the model and the describe operations from the first controller
		ModelNode modelA = servicesA.readWholeModel();
		ModelNode describeOp = new ModelNode();
		describeOp.get(OP).set(DESCRIBE);
		describeOp.get(OP_ADDR).set(
				PathAddress.pathAddress(
						PathElement.pathElement(SUBSYSTEM,
								ZooKeeperExtension.SUBSYSTEM_NAME))
						.toModelNode());
		List<ModelNode> operations = super.checkResultAndGetContents(
				servicesA.executeOperation(describeOp)).asList();

		// Install the describe options from the first controller into a second
		// controller
		KernelServicesBuilder builderB = createKernelServicesBuilder(AdditionalInitialization.MANAGEMENT);
		builderB.setBootOperations(operations);
		KernelServices servicesB = builderB.build();
		ModelNode modelB = servicesB.readWholeModel();

		// Make sure the models from the two controllers are identical
		super.compare(modelA, modelB);
	}

	/**
	 * Tests that the subsystem can be removed
	 */
	//@Test
	public void testSubsystemRemoval() throws Exception {
		AdditionalInitialization additionalInit = new AdditionalInitialization();
		KernelServicesBuilder builder = createKernelServicesBuilder(additionalInit);
		
		// Parse the subsystem xml and install into the first controller
		String subsystemXml =  "subsystem.xml";
		
		builder.setSubsystemXmlResource(subsystemXml);
		KernelServices services = builder.build();
		// Checks that the subsystem was removed from the model
		super.assertRemoveSubsystemResources(services);

		// TODO Check that any services that were installed were removed here
	}
	
	protected String getSubsystemXml() throws IOException {
		return readResource("subsystem.xml");
	}
}
