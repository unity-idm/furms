/*
 * Copyright (c) 2021 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.rest.admin;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.imunity.furms.TestBeansRegistry;
import io.imunity.furms.core.config.security.SecurityProperties;
import io.imunity.furms.core.config.security.WebAppSecurityConfiguration;
import io.imunity.furms.domain.users.FURMSUser;
import io.imunity.furms.domain.users.PersistentId;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.Base64;
import java.util.Optional;
import java.util.UUID;

import static java.lang.String.format;
import static java.time.ZoneOffset.UTC;
import static org.mockito.Mockito.when;

@WebMvcTest(controllers = {
			CommunityRestController.class,
			ProjectsRestController.class,
			SitesRestController.class},
		excludeFilters = {@ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, value = WebAppSecurityConfiguration.class)},
		includeFilters = {@ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, value = SecurityProperties.class),
							@ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, value = ObjectMapper.class)})
abstract class RestApiControllerIntegrationTest extends TestBeansRegistry {

	@Autowired
	protected MockMvc mockMvc;

	@Autowired
	protected ObjectMapper objectMapper;

	@MockBean
	protected CommunityRestService communityRestService;

	@MockBean
	protected ProjectsRestService projectsRestService;

	@MockBean
	protected SitesRestService sitesRestService;

	private final PersistentId userId = new PersistentId("userId");
	private final UUID apiKey = UUID.randomUUID();

	protected final User sampleUser = new User("fenixIdentifier", "title", "firstname", "lastname", "email",
			new Affiliation("name", "email", "country", "postalAddress"), "nationality",
			"phone", LocalDate.MIN, "placeOfBirth", "postalAddress");
	protected final ZonedDateTime sampleFrom = ZonedDateTime.of(LocalDateTime.of(2000, 01, 01, 01, 01), UTC);
	protected final ZonedDateTime sampleTo = ZonedDateTime.of(LocalDateTime.of(2000, 12, 31, 23, 59), UTC);

	@BeforeEach
	void setup() throws Exception {
		when(userApiKeyService.findUserByUserIdAndApiKey(userId, apiKey))
				.thenReturn(Optional.of(FURMSUser.builder()
						.id(userId)
						.email("email@domain.com")
						.build()));
	}

	protected String authKey() {
		final byte[] userPass = format("%s:%s", userId.id, apiKey).getBytes();
		final String token = Base64.getEncoder().encodeToString(userPass);
		return "Basic " + token;
	}
}
