/*
 * Copyright (c) 2021 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.rest.error;

import io.imunity.furms.TestBeansRegistry;
import io.imunity.furms.core.config.security.SecurityProperties;
import io.imunity.furms.core.config.security.WebAppSecurityConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = {ExceptionHandlersStubController.class},
		excludeFilters = {@ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, value = WebAppSecurityConfiguration.class)},
		includeFilters = {@ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, value = SecurityProperties.class)})
public class RestExceptionHandlersTest extends TestBeansRegistry {

	@Autowired
	private MockMvc mockMvc;

	@Test
	void shouldReturn_404_onUnknownUserException() throws Exception {
		mockMvc.perform(get("/test/exception/handler/unknown-user-exception"))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.error", is("Not Found")));
	}

	@Test
	void shouldHandleDefaultSpringExceptionMethodNotAllowed() throws Exception {
		mockMvc.perform(post("/test/exception/handler/runtime-exception"))
				.andExpect(status().isMethodNotAllowed())
				.andExpect(jsonPath("$.error", is("Method Not Allowed")));
	}

	@Test
	void shouldReturn_500_onRuntimeException() throws Exception {
		mockMvc.perform(get("/test/exception/handler/runtime-exception"))
				.andExpect(status().isInternalServerError())
				.andExpect(jsonPath("$.error", is("Internal Server Error")));
	}

	@Test
	void shouldReturn_500_onException() throws Exception {
		mockMvc.perform(get("/test/exception/handler/exception"))
				.andExpect(status().isInternalServerError())
				.andExpect(jsonPath("$.error", is("Internal Server Error")));
	}

}
