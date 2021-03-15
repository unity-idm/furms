/*
 * Copyright (c) 2021 Bixbit - Krzysztof Benedyczak. All rights reserved.
 * See LICENCE.txt file for licensing information.
 */
package io.imunity.furms.cidp;

import io.imunity.furms.api.users.UserService;
import io.imunity.furms.core.config.security.SecurityProperties;
import io.imunity.furms.core.config.security.WebAppSecurityConfiguration;
import io.imunity.furms.domain.users.FenixUserId;
import io.imunity.furms.domain.users.UserStatus;
import io.imunity.furms.rest.cidp.CentralIdPRestAPIController;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan.Filter;
import org.springframework.context.annotation.FilterType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.CoreMatchers.containsString;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = {CentralIdPRestAPIController.class}, 
	excludeFilters = {@Filter(type = FilterType.ASSIGNABLE_TYPE, value = WebAppSecurityConfiguration.class)},
	includeFilters = {@Filter(type = FilterType.ASSIGNABLE_TYPE, value = SecurityProperties.class)})
public class APIAuthNTest {

	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private UserService userService;
	
	@Test
	public void shouldDenyWithoutAuthn() throws Exception {
		this.mockMvc.perform(get("/rest-api/v1/cidp/user/F_ID/status"))
			.andExpect(status().isUnauthorized());
	}
	
	@Test
	public void shouldDenyWithInvalidUsername() throws Exception {
		when(userService.getUserStatus(new FenixUserId("F_ID"))).thenReturn(UserStatus.ENABLED);
		
		this.mockMvc.perform(get("/rest-api/v1/cidp/user/F_ID/status").with(httpBasic("WRONG", "cidppass")))
			.andExpect(status().isUnauthorized());
	}

	@Test
	public void shouldDenyWithInvalidPassword() throws Exception {
		when(userService.getUserStatus(new FenixUserId("F_ID"))).thenReturn(UserStatus.ENABLED);
		
		this.mockMvc.perform(get("/rest-api/v1/cidp/user/F_ID/status").with(httpBasic("cidp", "WRONG")))
			.andExpect(status().isUnauthorized());
	}

	@Test
	public void shouldAllowWithValidCredential() throws Exception {
		when(userService.getUserStatus(new FenixUserId("F_ID"))).thenReturn(UserStatus.ENABLED);
		this.mockMvc.perform(get("/rest-api/v1/cidp/user/F_ID/status").with(httpBasic("cidp", "cidppass")))
			.andDo(print()).andExpect(status().isOk())
			.andExpect(content().string(containsString("{\"status\":\"ENABLED\"}")));
	}
}
