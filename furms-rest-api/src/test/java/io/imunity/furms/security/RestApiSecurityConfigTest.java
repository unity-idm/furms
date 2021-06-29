/*
 * Copyright (c) 2021 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.security;

import io.imunity.furms.TestBeansRegistry;
import io.imunity.furms.core.config.security.SecurityProperties;
import io.imunity.furms.core.config.security.WebAppSecurityConfiguration;
import io.imunity.furms.domain.users.FURMSUser;
import io.imunity.furms.domain.users.PersistentId;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Base64;
import java.util.Optional;
import java.util.UUID;

import static java.lang.String.format;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = {RestApiTestController.class},
		excludeFilters = {@ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, value = WebAppSecurityConfiguration.class)},
		includeFilters = {@ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, value = SecurityProperties.class)})
public class RestApiSecurityConfigTest extends TestBeansRegistry {

	@Autowired
	private MockMvc mockMvc;

	@Test
	void shouldReturn403onRequestWithoutCredentials() throws Exception {
		mockMvc.perform(get("/rest-api/v1/test"))
				.andExpect(status().isUnauthorized());
	}

	@Test
	void shouldReturn200onRequestWithCredentials() throws Exception {
		final PersistentId userId = new PersistentId("userId");
		final UUID apiKey = UUID.randomUUID();

		when(userApiKeyService.findUserByUserIdAndApiKey(userId, apiKey))
				.thenReturn(Optional.of(FURMSUser.builder()
						.id(userId)
						.email("email@domain.com")
						.build()));

		mockMvc.perform(get("/rest-api/v1/test")
				.header(HttpHeaders.AUTHORIZATION, convertToAuthHeader(userId, apiKey)))
				.andExpect(status().isOk());
	}

	@Test
	void shouldNotAuthorizeWhenUserOrApiAreWrong() throws Exception {
		final PersistentId userId = new PersistentId("userId");
		final UUID apiKey = UUID.randomUUID();
		final PersistentId wrongUserId = new PersistentId("wrongUserId");
		final UUID wrongApiKey = UUID.randomUUID();

		when(userApiKeyService.findUserByUserIdAndApiKey(userId, apiKey))
				.thenReturn(Optional.of(FURMSUser.builder()
						.id(userId)
						.email("email@domain.com")
						.build()));

		mockMvc.perform(get("/rest-api/v1/test")
				.header(HttpHeaders.AUTHORIZATION, convertToAuthHeader(wrongUserId, apiKey)))
				.andExpect(status().isUnauthorized());
		mockMvc.perform(get("/rest-api/v1/test")
				.header(HttpHeaders.AUTHORIZATION, convertToAuthHeader(userId, wrongApiKey)))
				.andExpect(status().isUnauthorized());
	}

	@Test
	void shouldNotAuthorizeWhenUserApiKeyReasonDoesntExists() throws Exception {
		final PersistentId userId = new PersistentId("userId");
		final UUID apiKey = UUID.randomUUID();

		when(userApiKeyService.findUserByUserIdAndApiKey(userId, apiKey))
				.thenReturn(Optional.empty());

		mockMvc.perform(get("/rest-api/v1/test")
				.header(HttpHeaders.AUTHORIZATION, convertToAuthHeader(userId, apiKey)))
				.andExpect(status().isUnauthorized());
	}

	@Test
	void shouldIgnoreAuthorizationWhenIsCIDPController() throws Exception {
		final PersistentId userId = new PersistentId("userId");
		final UUID apiKey = UUID.randomUUID();

		when(userApiKeyService.findUserByUserIdAndApiKey(userId, apiKey))
				.thenReturn(Optional.of(FURMSUser.builder()
						.id(userId)
						.email("email@domain.com")
						.build()));

		mockMvc.perform(get("/rest-api/v1/cidp/test")
				.header(HttpHeaders.AUTHORIZATION, convertToAuthHeader(userId, apiKey)))
				.andExpect(status().isUnauthorized());
	}

	private String convertToAuthHeader(PersistentId username, UUID password) {
		final byte[] userPass = format("%s:%s", username.id, password.toString()).getBytes();
		final String token = Base64.getEncoder().encodeToString(userPass);
		return "Basic " + token;
	}

}
