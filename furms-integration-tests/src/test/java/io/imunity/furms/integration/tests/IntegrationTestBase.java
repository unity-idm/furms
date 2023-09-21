/*
 * Copyright (c) 2021 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.integration.tests;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import io.imunity.furms.integration.tests.tools.users.TestUser;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.util.TestSocketUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.RequestBuilder;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static io.imunity.furms.integration.tests.tools.users.TestUsersProvider.fenixAdmin;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

@AutoConfigureMockMvc
@SpringBootTest(classes = FumrsIntegrationTestsServer.class)
public class IntegrationTestBase extends DBTestManager {

	private final static Integer SERVER_PORT = TestSocketUtils.findAvailableTcpPort();

	protected final WireMockServer server = new WireMockServer(options().port(SERVER_PORT));

	@Autowired
	protected MockMvc mockMvc;

	@Autowired
	protected ObjectMapper objectMapper;

	protected static TestUser ADMIN_USER;

	@BeforeAll
	static void init() {
		System.setProperty("server.port", SERVER_PORT.toString());
		System.setProperty("furms.unity.admin-url", "http://localhost:"+SERVER_PORT+"/unity");
		System.setProperty("furms.url", "http://localhost:"+SERVER_PORT);
	}

	@BeforeEach
	protected void setUpInternal() throws JsonProcessingException {
		server.start();

		ADMIN_USER = fenixAdmin();
		ADMIN_USER.registerUserMock(server);
		ADMIN_USER.registerApiKey(userApiKeyRepository);
	}

	@AfterEach
	protected void tearDownInternal() {
		super.tearDown();
		server.resetMappings();
		server.stop();
	}

	protected void setupUser(TestUser user) throws JsonProcessingException {
		user.setupUser(server, userApiKeyRepository);
	}

	protected RequestBuilder adminGET(String url, Object... vars) {
		return get(url, vars)
				.with(ADMIN_USER.getHttpBasic());
	}

	protected RequestBuilder adminPOST(String url, Object... vars) {
		return post(url, vars)
				.with(ADMIN_USER.getHttpBasic());
	}

	protected RequestBuilder adminDELETE(String url, Object... vars) {
		return delete(url, vars)
			.with(ADMIN_USER.getHttpBasic());
	}
}