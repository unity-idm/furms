/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */
package io.imunity.furms.broker.runner;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.apache.qpid.server.SystemLauncher;

import com.google.common.collect.ImmutableMap;

public class QpidBrokerJRunner {

	 private static final String KEYSTORE_PATH = QpidBrokerJRunner.class
			 .getResource("/qpid.p12").getPath();
	 
	public static void main(String[] args) throws Exception {
		run(44444, "configuration.json");
	}

	public static void run(int port, String configFileName) throws Exception {
		Map<String, Object> attributes = new HashMap<>();
		URL initialConfig = QpidBrokerJRunner.class.getClassLoader().getResource(configFileName);
		attributes.put("type", "Memory");
		attributes.put("initialConfigurationLocation", initialConfig.toExternalForm());
		attributes.put("startupLoggedToSystemOut", true);
		attributes.put("context", ImmutableMap.of(
				"qpid.amqp_port", port,
				"qpid.keystore", KEYSTORE_PATH));

		SystemLauncher systemLauncher = new SystemLauncher();
		systemLauncher.startup(attributes);
	}

}