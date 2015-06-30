package org.jboss.as.zookeeper;

import org.jboss.as.subsystem.test.AbstractSubsystemBaseTest;
import org.jboss.as.zookeeper.ZooKeeperExtension;

import java.io.IOException;

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

}