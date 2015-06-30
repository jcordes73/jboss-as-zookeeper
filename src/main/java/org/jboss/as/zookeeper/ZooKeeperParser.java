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

import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.OP;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.OP_ADDR;

import java.util.List;

import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;

import org.jboss.as.controller.PathAddress;
import org.jboss.as.controller.PathElement;
import org.jboss.as.controller.descriptions.ModelDescriptionConstants;
import org.jboss.as.controller.parsing.ParseUtils;
import org.jboss.as.controller.persistence.SubsystemMarshallingContext;
import org.jboss.dmr.ModelNode;
import org.jboss.dmr.Property;
import org.jboss.staxmapper.XMLElementReader;
import org.jboss.staxmapper.XMLElementWriter;
import org.jboss.staxmapper.XMLExtendedStreamReader;
import org.jboss.staxmapper.XMLExtendedStreamWriter;

public class ZooKeeperParser implements XMLStreamConstants,
		XMLElementReader<List<ModelNode>>,
		XMLElementWriter<SubsystemMarshallingContext> {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void writeContent(XMLExtendedStreamWriter writer,
			SubsystemMarshallingContext context) throws XMLStreamException {
		ModelNode node = context.getModelNode();

		
		if (node.hasDefined(ZooKeeperExtension.SERVER)) {
			context.startSubsystemElement(ZooKeeperExtension.NAMESPACE, false);
			writer.writeStartElement(ZooKeeperExtension.SERVER);
			ModelNode server = node.get(ZooKeeperExtension.SERVER);

			for (Property property : server.asPropertyList()) {
				ModelNode entry = property.getValue();
				writer.writeAttribute("name", property.getName());

				ZooKeeperServerDefinition.TICK_TIME.marshallAsAttribute(entry,
						true, writer);
				ZooKeeperServerDefinition.DATA_DIR.marshallAsAttribute(entry,
						true, writer);
				ZooKeeperServerDefinition.SOCKET_BINDING.marshallAsAttribute(
						entry, true, writer);
			}
			writer.writeEndElement();
		} else {
			context.startSubsystemElement(ZooKeeperExtension.NAMESPACE, true);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void readElement(XMLExtendedStreamReader reader, List<ModelNode> list)
			throws XMLStreamException {

		// Require no attributes
		ParseUtils.requireNoAttributes(reader);

		ModelNode addSubsystemOperation = new ModelNode();
		addSubsystemOperation.get(OP).set(ModelDescriptionConstants.ADD);
		addSubsystemOperation.get(OP_ADDR).set(
				PathAddress.pathAddress(ZooKeeperExtension.SUBSYSTEM_PATH)
						.toModelNode());
		list.add(addSubsystemOperation);
		
		if (reader.hasNext() && reader.nextTag() != END_ELEMENT) {
			if (!reader.getLocalName().equals(ZooKeeperExtension.SERVER)) {
				throw ParseUtils.unexpectedElement(reader);
			}	

			readServer(reader, list);
		}
	}

	private void readServer(XMLExtendedStreamReader reader, List<ModelNode> list)
			throws XMLStreamException {
		ModelNode addServerOperation = new ModelNode();
		addServerOperation.get(OP).set(ModelDescriptionConstants.ADD);

		ZooKeeperServerDefinition.TICK_TIME.parseAndSetParameter(
				ZooKeeperServerDefinition.TICK_TIME.getDefaultValue()
						.asString(), addServerOperation, reader);
		ZooKeeperServerDefinition.DATA_DIR
				.parseAndSetParameter(ZooKeeperServerDefinition.DATA_DIR
						.getDefaultValue().asString(), addServerOperation,
						reader);

		ZooKeeperServerDefinition.SOCKET_BINDING.parseAndSetParameter(
				ZooKeeperServerDefinition.SOCKET_BINDING.getDefaultValue()
						.asString(), addServerOperation, reader);

		String serverName = "default";

		for (int i = 0; i < reader.getAttributeCount(); i++) {
			String attr = reader.getAttributeLocalName(i);
			String value = reader.getAttributeValue(i);

			if (attr.equals("name")) {
				serverName = value;

			} else if (attr.equals(ZooKeeperExtension.TICK_TIME)) {
				ZooKeeperServerDefinition.TICK_TIME.parseAndSetParameter(value,
						addServerOperation, reader);
			} else if (attr.equals(ZooKeeperExtension.DATA_DIR)) {
				ZooKeeperServerDefinition.DATA_DIR.parseAndSetParameter(value,
						addServerOperation, reader);
			} else if (attr.equals(ZooKeeperExtension.SOCKET_BINDING)) {
				ZooKeeperServerDefinition.SOCKET_BINDING.parseAndSetParameter(
						value, addServerOperation, reader);
			} else {
				throw ParseUtils.unexpectedAttribute(reader, i);
			}
		}

		PathAddress addr = PathAddress.pathAddress(
				ZooKeeperExtension.SUBSYSTEM_PATH,
				PathElement.pathElement(ZooKeeperExtension.SERVER, serverName));
		addServerOperation.get(OP_ADDR).set(addr.toModelNode());

		list.add(addServerOperation);
	}
}
