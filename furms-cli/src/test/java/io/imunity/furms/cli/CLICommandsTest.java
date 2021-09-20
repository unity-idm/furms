/*
 * Copyright (c) 2021 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.cli;

import static com.github.tomakehurst.wiremock.client.WireMock.any;
import static com.github.tomakehurst.wiremock.client.WireMock.status;
import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching;
import static io.imunity.furms.cli.ConfigParameterNames.FURMS_URL;
import static io.imunity.furms.cli.ConfigParameterNames.TRUSTSTORE_PATH;
import static io.imunity.furms.cli.client.RestTemplateConfig.FURMS_REST_BASE_PATH;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.util.SocketUtils;
import org.springframework.web.util.UriComponentsBuilder;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.matching.UrlPattern;


public abstract class CLICommandsTest {

	protected static final String DEFAULT_DEFINITION_FILE_BODY = "{\"testField\":\"testValue\"}";

	protected ObjectMapper objectMapper = new ObjectMapper();

	private static int PORT = SocketUtils.findAvailableTcpPort();

	protected static WireMockServer server = new WireMockServer(PORT);

	@BeforeAll
	static void beforeAll() {
		server.addStubMapping(any(UrlPattern.ANY)
				.willReturn(status(200))
				.build());
	}

	@BeforeEach
	void setUp() {
		server.start();
	}

	@AfterEach
	void tearDown() {
		server.stop();
	}

	protected void runCLI(String... args) {
		final List<String> defaultParams = List.of(
				FURMS_URL, "http://localhost:"+PORT,
				TRUSTSTORE_PATH, loadResourcePath("testTruststore.jks"),
				"--config-file", loadResourcePath("config.properties"));
		final List<String> params = new ArrayList<>();
		params.addAll(List.of(args));
		params.addAll(defaultParams);

		CLIApplication.main(params.toArray(new String[params.size()]));
	}

	protected UrlPattern restPath(String path, Object... params) {
		return urlMatching(UriComponentsBuilder
				.fromPath(FURMS_REST_BASE_PATH + path)
				.build((Object[])params)
				.toString());
	}

	protected File createDefaultDefinitionFile() {
		try {
			final String path = Paths.get(UUID.randomUUID().toString() + ".json").toAbsolutePath().toString();
			final File file = new File(path);
			file.createNewFile();
			FileUtils.write(file, DEFAULT_DEFINITION_FILE_BODY);

			return file;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	private String loadResourcePath(String name) {
		return CLICommandsTest.class.getClassLoader().getResource(name).getPath();
	}
}