/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.unity.client;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder;
import com.github.tomakehurst.wiremock.matching.EqualToPattern;
import com.github.tomakehurst.wiremock.matching.RequestPattern;
import com.github.tomakehurst.wiremock.matching.RequestPatternBuilder;
import com.github.tomakehurst.wiremock.matching.UrlPattern;
import com.github.tomakehurst.wiremock.stubbing.StubMapping;
import com.github.tomakehurst.wiremock.verification.VerificationResult;
import io.imunity.furms.spi.notifications.NotificationDAO;
import io.imunity.furms.spi.sites.SiteRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.util.SocketUtils;

import java.util.Map;

import static com.github.tomakehurst.wiremock.http.RequestMethod.DELETE;
import static com.github.tomakehurst.wiremock.http.RequestMethod.GET;
import static com.github.tomakehurst.wiremock.http.RequestMethod.POST;
import static com.github.tomakehurst.wiremock.http.RequestMethod.PUT;
import static java.lang.Boolean.TRUE;
import static org.apache.http.HttpStatus.SC_OK;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@SpringBootTest
class UnityClientTest {

	private final static int SERVER_PORT = SocketUtils.findAvailableTcpPort();

	private final WireMockServer server = new WireMockServer(SERVER_PORT);

	@Autowired
	private UnityClient unityClient;

	@MockBean
	private SiteRepository siteRepository;
	@MockBean
	private NotificationDAO emailNotificationDAO;

	@BeforeAll
	static void init() {
		System.setProperty("furms.unity.admin-url", createWireMockUrl(null));
	}

	@BeforeEach
	void setUp() {
		server.start();
	}

	@AfterEach
	void tearDown() {
		server.resetMappings();
		server.stop();
	}

	@Test
	void shouldSend_GET_toUnityServer() {
		//given
		RequestPattern request = new RequestPatternBuilder(GET,
				new UrlPattern(new EqualToPattern("/path/to/unity/test?param1=stringValue&param2=1"), false))
				.build();
		server.addStubMapping(new StubMapping(request, ResponseDefinitionBuilder.responseDefinition()
				.withBody("{\"field\": \"string_value\"}")
				.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
				.withStatus(SC_OK)
				.build()));

		//when
		SampleDto response = unityClient.get("/path/to/unity/test", SampleDto.class,
				Map.of("param1", "stringValue", "param2", "1"));

		//then
		VerificationResult verificationResult = server.countRequestsMatching(request);
		assertThat(verificationResult.getCount()).isEqualTo(1);
		assertThat(response.getField()).isEqualTo("string_value");
	}

	@Test
	void shouldSend_POST_toUnityServer() {
		//given
		RequestPattern request = new RequestPatternBuilder(
				POST, new UrlPattern(new EqualToPattern("/path/to/unity/test"), false)).build();
		server.addStubMapping(new StubMapping(request, ResponseDefinitionBuilder.responseDefinition()
				.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
				.withBody("{\"field\": \"string\"}")
				.withStatus(SC_OK)
				.build()));

		//when
		unityClient.post("/path/to/unity/test", new SampleDto("string"));

		//then
		VerificationResult verificationResult = server.countRequestsMatching(request);
		assertThat(verificationResult.getCount()).isEqualTo(1);
	}

	@Test
	void shouldSend_PUT_toUnityServer() {
		//given
		RequestPattern request = new RequestPatternBuilder(
				PUT, new UrlPattern(new EqualToPattern("/path/to/unity/test"), false)).build();
		server.addStubMapping(new StubMapping(request, ResponseDefinitionBuilder.responseDefinition()
				.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
				.withBody("{\"field\": \"string\"}")
				.withStatus(SC_OK)
				.build()));

		//when
		unityClient.put("/path/to/unity/test", Map.of("param", "test"));

		//then
		VerificationResult verificationResult = server.countRequestsMatching(request);
		assertThat(verificationResult.getCount()).isEqualTo(1);
	}

	@Test
	void shouldSend_DELETE_toUnityServer() {
		//given
		RequestPattern request = new RequestPatternBuilder(
					DELETE,
					new UrlPattern(new EqualToPattern("/path/to/unity/test?recursive=true"), false))
				.build();
		server.addStubMapping(new StubMapping(request, ResponseDefinitionBuilder.responseDefinition()
				.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
				.withStatus(SC_OK)
				.build()));

		//when
		unityClient.delete("/path/to/unity/test", Map.of("recursive", TRUE.toString()));

		//then
		VerificationResult verificationResult = server.countRequestsMatching(request);
		assertThat(verificationResult.getCount()).isEqualTo(1);
	}

	private static String createWireMockUrl(String path) {
		String base = "http://localhost:"+SERVER_PORT;
		if (path == null) {
			return base;
		}
		return base + path;

	}

	private static class SampleDto {

		private final String field;

		@JsonCreator
		public SampleDto(@JsonProperty("field") String field) {
			this.field = field;
		}

		public String getField() {
			return field;
		}
	}

}