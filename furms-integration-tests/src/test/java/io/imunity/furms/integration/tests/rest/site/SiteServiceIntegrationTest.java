/*
 * Copyright (c) 2021 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.integration.tests.rest.site;

import io.imunity.furms.domain.policy_documents.PolicyId;
import io.imunity.furms.domain.services.InfraServiceId;
import io.imunity.furms.domain.sites.Site;
import io.imunity.furms.domain.sites.SiteExternalId;
import io.imunity.furms.domain.sites.SiteId;
import io.imunity.furms.integration.tests.IntegrationTestBase;
import io.imunity.furms.integration.tests.tools.users.TestUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.UUID;

import static io.imunity.furms.integration.tests.tools.DefaultDataBuilders.defaultPolicy;
import static io.imunity.furms.integration.tests.tools.DefaultDataBuilders.defaultService;
import static io.imunity.furms.integration.tests.tools.DefaultDataBuilders.defaultSite;
import static io.imunity.furms.integration.tests.tools.users.TestUsersProvider.basicUser;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.in;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class SiteServiceIntegrationTest extends IntegrationTestBase {

	private Site site;
	private Site darkSite;

	@BeforeEach
	void setUp() {
		Site.SiteBuilder siteBuilder = defaultSite().name("site1");
		site = siteBuilder
				.id(siteRepository.create(siteBuilder.build(), siteBuilder.build().getExternalId()))
				.build();
		Site.SiteBuilder darkSiteBuilder = defaultSite()
				.name("Dark Site");
		darkSite = darkSiteBuilder
				.id(siteRepository.create(darkSiteBuilder.build(), new SiteExternalId("dsid")))
				.build();
	}

	@Test
	void shouldFindAllSiteServicesForSpecificSite() throws Exception {
		//given
		final InfraServiceId service1 = createInfraService(site.getId(), "Test 1");
		final InfraServiceId service2 = createInfraService(site.getId(),"Test 2");
		createInfraService(darkSite.getId(),"Test 3");

		//when
		mockMvc.perform(adminGET("/rest-api/v1/sites/{siteId}/services", site.getId().id))
				.andDo(print())
				.andExpect(status().isOk())
				.andExpect(jsonPath("$", hasSize(2)))
				.andExpect(jsonPath("$.[0].serviceId", in(Set.of(service1.id.toString(), service2.id.toString()))))
				.andExpect(jsonPath("$.[0].name", in(Set.of("Test 1", "Test 2"))))
				.andExpect(jsonPath("$.[0].policyId", notNullValue()))
				.andExpect(jsonPath("$.[1].serviceId", in(Set.of(service1.id.toString(), service2.id.toString()))))
				.andExpect(jsonPath("$.[1].name", in(Set.of("Test 1", "Test 2"))))
				.andExpect(jsonPath("$.[1].policyId", notNullValue()));
	}

	@Test
	void shouldReturnNotFoundIfSiteDoesNotExistsWhileGettingServices() throws Exception {
		//when
		mockMvc.perform(adminGET("/rest-api/v1/sites/{siteId}/services", UUID.randomUUID().toString()))
				.andDo(print())
				.andExpect(status().isNotFound());
	}

	@Test
	void shouldReturnEmptyArrayWhenThereAreNoBelongsServices() throws Exception {
		//when
		mockMvc.perform(adminGET("/rest-api/v1/sites/{siteId}/services", site.getId().id))
				.andDo(print())
				.andExpect(status().isOk())
				.andExpect(jsonPath("$").isArray())
				.andExpect(jsonPath("$").isEmpty());
	}


	@Test
	void shouldReturnForbiddenIfUserDoesNotBelongsToSite() throws Exception {
		//given
		final TestUser testUser = basicUser();
		testUser.addSiteAdmin(site.getId());
		setupUser(testUser);

		//when
		mockMvc.perform(get("/rest-api/v1/sites/{siteId}/services", darkSite.getId().id)
				.with(testUser.getHttpBasic()))
				.andDo(print())
				.andExpect(status().isForbidden());
	}

	@Test
	void shouldFindSiteServiceThatBelongsToSite() throws Exception {
		//given
		final InfraServiceId service = createInfraService(site.getId(), "Test 1");
		createInfraService(darkSite.getId(),"Test 2");

		//when
		mockMvc.perform(adminGET("/rest-api/v1/sites/{siteId}/services/{serviceId}", site.getId().id, service.id))
				.andDo(print())
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.serviceId", equalTo(service.id.toString())))
				.andExpect(jsonPath("$.name", equalTo("Test 1")))
				.andExpect(jsonPath("$.policyId", notNullValue()));
	}

	@Test
	void shouldReturnForbiddenIfSiteServiceNotBelongsToSite() throws Exception {
		//given
		final InfraServiceId service = createInfraService(site.getId(), "Test 1");
		createInfraService(darkSite.getId(),"Test 2");

		//when
		mockMvc.perform(adminGET("/rest-api/v1/sites/{siteId}/services/{serviceId}", darkSite.getId().id, service.id))
				.andDo(print())
				.andExpect(status().isForbidden());
	}

	@Test
	void shouldReturnNotFoundIfSiteServiceDoesNotExistsWhileGettingSiteService() throws Exception {
		final String service = UUID.randomUUID().toString();
		//when
		mockMvc.perform(adminGET("/rest-api/v1/sites/{siteId}/services/{serviceId}", site.getId().id, service))
				.andDo(print())
				.andExpect(status().isNotFound());
	}

	@Test
	void shouldReturnNotFoundIfSiteDoesNotExistsWhileGettingSiteService() throws Exception {
		final InfraServiceId service = createInfraService(site.getId(), "Test 1");
		//when
		mockMvc.perform(adminGET("/rest-api/v1/sites/{siteId}/services/{serviceId}", UUID.randomUUID().toString(), service.id))
				.andDo(print())
				.andExpect(status().isNotFound());
	}

	private InfraServiceId createInfraService(SiteId siteId, String name) {
		final PolicyId policyId = policyDocumentRepository.create(defaultPolicy()
				.siteId(siteId)
				.build());
		return infraServiceRepository.create(defaultService()
				.siteId(siteId)
				.name(name)
				.policyId(policyId)
				.build());
	}

}
