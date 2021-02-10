/*
 * Copyright (c) 2021 Bixbit - Krzysztof Benedyczak. All rights reserved.
 * See LICENCE.txt file for licensing information.
 */
package io.imunity.furms.cidp;

import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan.Filter;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import io.imunity.furms.api.users.UserService;
import io.imunity.furms.core.config.security.SecurityProperties;
import io.imunity.furms.core.config.security.WebAppSecurityConfiguration;
import io.imunity.furms.domain.users.UserStatus;
import io.imunity.furms.rest.cidp.CentralIdPRestAPIController;

@WebMvcTest(controllers = {CentralIdPRestAPIController.class}, 
	excludeFilters = {@Filter(type = FilterType.ASSIGNABLE_TYPE, value = WebAppSecurityConfiguration.class)},
	includeFilters = {@Filter(type = FilterType.ASSIGNABLE_TYPE, value = SecurityProperties.class)})
public class CentralIdPRestAPIControllerTest {

	@Autowired
	private MockMvc mockMvc;
	
	@MockBean
	private UserService userService;
	
	@Test
	public void shouldSetStatus() throws Exception {
		this.mockMvc.perform(post("/rest-api/v1/cidp/user/F_ID/status")
				.with(httpBasic("cidp", "cidppass"))
				.contentType(MediaType.APPLICATION_JSON).content("{\"status\":\"ENABLED\"}"))
			.andExpect(status().isOk());
		
		verify(userService).setUserStatus("F_ID", UserStatus.ENABLED);
	}
}
