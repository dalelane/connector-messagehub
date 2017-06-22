/*******************************************************************************
 * Copyright (c) 2015 IBM Corp.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Eclipse Distribution License v1.0 which accompany this distribution.
 *
 * The Eclipse Public License is available at
 *    http://www.eclipse.org/legal/epl-v10.html
 * and the Eclipse Distribution License is available at
 *   http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * Contributors:
 *    Leo Davison - initial implementation
 */

package com.ibm.iotf.connector;

import java.io.File;
import java.io.IOException;
import java.util.Properties;

import com.ibm.iotf.connector.impl.publisher.MHubPublisher;
import com.ibm.iotf.connector.impl.publisher.MessageHubEnvironment;
import com.ibm.iotf.connector.impl.subscriber.IoTFEnvironment;
import com.ibm.iotf.connector.impl.subscriber.IoTFSubscriber;

public class Connector {

	private IoTFSubscriber subscriber = null;

	/**
	 * Starts a new IoTP connector.
	 *
	 * @param connectorId - Unique ID for the connector
	 * @param iotpInstance - Location and credentials of the IoTP instance to connect to
	 * @param iotpDeviceId - ID of the device to connect events from
	 * @param messagehubInstance - Location and credentials of the Message Hub instance to connect to
	 * @param jaasConf - Config file with credentials for accessing Message Hub
	 * @param trustStore - Location of the SSL trust store to use for this connector
	 */
	public void run(String connectorId,
			IoTFEnvironment iotpInstance, String iotpDeviceId,
			MessageHubEnvironment messagehubInstance, File jaasConf, File trustStore) throws IOException {

		subscriber = new IoTFSubscriber(iotpInstance, iotpDeviceId);
		MHubPublisher publisher = new MHubPublisher(messagehubInstance,
				getMessageHubClientConfiguration(connectorId, messagehubInstance, trustStore));

		// configure the subscriber to hand off events to the publisher
		subscriber.setPublisher(publisher);

		subscriber.run();
	}

	private Properties getMessageHubClientConfiguration(String connectorId, MessageHubEnvironment messagehubInstance, File trustStore) {
		Properties props = new Properties();
		props.put("key.serializer", "org.apache.kafka.common.serialization.ByteArraySerializer");
		props.put("value.serializer", "org.apache.kafka.common.serialization.ByteArraySerializer");
		props.put("bootstrap.servers", messagehubInstance.getCredentials().getKafkaBrokersSasl()[0]);
		props.put("client.id", connectorId);
		props.put("acks", "all");
		props.put("retries", "2");
		props.put("max.block.ms", 15_000);
		props.put("max.in.flight.requests.per.connection", 50);
		props.put("max.request.size", 1000012);
		props.put("sasl.mechanism", "PLAIN");
		props.put("security.protocol", "SASL_SSL");
		props.put("ssl.protocol", "TLSv1.2");
		props.put("ssl.enabled.protocols", "TLSv1.2");
		props.put("ssl.truststore.location", trustStore.getAbsolutePath());
		props.put("ssl.truststore.password", "changeit"); // System.getProperty("javax.net.ssl.keyStorePassword"));
		props.put("ssl.truststore.type", "JKS");
		props.put("ssl.endpoint.identification.algorithm", "HTTPS");
		return props;
	}
}
