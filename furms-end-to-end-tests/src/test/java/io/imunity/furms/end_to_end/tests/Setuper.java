/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */
package io.imunity.furms.end_to_end.tests;

import com.google.common.collect.ImmutableMap;
import org.apache.qpid.server.SystemLauncher;
import org.apache.qpid.server.model.SystemConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.scheduling.annotation.EnableScheduling;
import pl.edu.icm.unity.MessageSource;
import pl.edu.icm.unity.engine.server.UnityApplication;

import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

@SpringBootApplication(scanBasePackages = "io.imunity.furms")
@EnableScheduling
@ConfigurationPropertiesScan("io.imunity.furms")
public class Setuper {
	public static void main(String[] args) throws Exception {
		UnityApplication theServer = new UnityApplication(MessageSource.PROFILE_FAIL_ON_MISSING);
		theServer.run(new String[] {"src/test/resources/unityServer.conf"});

		runQpid(44444, "configuration.json");

		SpringApplication.run(Setuper.class, args);
	}

	private static void runQpid(int port, String configFileName) throws Exception {
		Map<String, Object> attributes = new HashMap<>();
		URL initialConfig = Setuper.class.getClassLoader().getResource(configFileName);
		attributes.put("type", "Memory");
		attributes.put("initialConfigurationLocation", initialConfig.toExternalForm());
		attributes.put("startupLoggedToSystemOut", true);
		attributes.put("context", ImmutableMap.of(
			"qpid.amqp_port", port,
			"qpid.keystore", KEYSTORE_PATH,
			SystemConfig.QPID_WORK_DIR, Path.of("target", "qpid-workdir").toString()));

		SystemLauncher systemLauncher = new SystemLauncher();
		systemLauncher.startup(attributes);
	}

	private static final String KEYSTORE_PATH = getKeystorePath();

	private static String getKeystorePath() {
		try {
			return Setuper.class.getResource("/qpid.p12").toURI().toString();
		} catch (URISyntaxException e) {
			throw new RuntimeException(e);
		}
	}
}