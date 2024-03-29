/*
 * Copyright (c) 2021 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.integration.tests.rest.site;

import io.imunity.furms.domain.policy_documents.PolicyId;
import io.imunity.furms.domain.resource_types.ResourceTypeId;
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
import static io.imunity.furms.integration.tests.tools.DefaultDataBuilders.defaultResourceType;
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

public class SiteResourceTypeIntegrationTest extends IntegrationTestBase {

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
	void shouldFindAllResourceCreditsForSpecificSite() throws Exception {
		//given
		final ResourceTypeId resourceType1 = createResourceType(site.getId(), "Test 1");
		final ResourceTypeId resourceType2 = createResourceType(site.getId(),"Test 2");
		createResourceType(darkSite.getId(),"Test 3");

		//when
		mockMvc.perform(adminGET("/rest-api/v1/sites/{siteId}/resourceTypes", site.getId().id))
				.andDo(print())
				.andExpect(status().isOk())
				.andExpect(jsonPath("$", hasSize(2)))
				.andExpect(jsonPath("$.[0].typeId", in(Set.of(resourceType1.id.toString(), resourceType2.id.toString()))))
				.andExpect(jsonPath("$.[0].name", in(Set.of("Test 1", "Test 2"))))
				.andExpect(jsonPath("$.[0].serviceId", notNullValue()))
				.andExpect(jsonPath("$.[1].typeId", in(Set.of(resourceType1.id.toString(), resourceType2.id.toString()))))
				.andExpect(jsonPath("$.[1].name", in(Set.of("Test 1", "Test 2"))))
				.andExpect(jsonPath("$.[1].serviceId", notNullValue()));
	}

	@Test
	void shouldReturnNotFoundIfSiteDoesNotExistsWhileGettingResourceTypes() throws Exception {
		//when
		mockMvc.perform(adminGET("/rest-api/v1/sites/{siteId}/resourceTypes", UUID.randomUUID().toString()))
				.andDo(print())
				.andExpect(status().isNotFound());
	}

	@Test
	void shouldReturnEmptyArrayWhenThereAreNoBelongsResourceTypes() throws Exception {
		//when
		mockMvc.perform(adminGET("/rest-api/v1/sites/{siteId}/resourceTypes", site.getId().id))
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
		mockMvc.perform(get("/rest-api/v1/sites/{siteId}/resourceTypes", darkSite.getId().id)
				.with(testUser.getHttpBasic()))
				.andDo(print())
				.andExpect(status().isForbidden());
	}

	@Test
	void shouldFindResourceTypeThatBelongsToSite() throws Exception {
		//given
		final ResourceTypeId resourceType = createResourceType(site.getId(), "Test 1");
		createResourceType(darkSite.getId(),"Test 2");

		//when
		mockMvc.perform(adminGET("/rest-api/v1/sites/{siteId}/resourceTypes/{resourceTypeId}", site.getId().id, resourceType.id))
				.andDo(print())
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.typeId", equalTo(resourceType.id.toString())))
				.andExpect(jsonPath("$.name", equalTo("Test 1")))
				.andExpect(jsonPath("$.serviceId", notNullValue()));
	}

	@Test
	void shouldReturnForbiddenIfResourceTypeNotBelongsToSite() throws Exception {
		//given
		final ResourceTypeId resourceType = createResourceType(site.getId(), "Test 1");
		createResourceType(darkSite.getId(),"Test 2");

		final TestUser testUser = basicUser();
		testUser.addSiteAdmin(darkSite.getId());
		setupUser(testUser);

		//when
		mockMvc.perform(get("/rest-api/v1/sites/{siteId}/resourceTypes/{resourceTypeId}", darkSite.getId().id, resourceType.id)
				.with(testUser.getHttpBasic()))
				.andDo(print())
				.andExpect(status().isForbidden());
	}

	@Test
	void shouldReturnNotFoundIfResourceTypeDoesNotExists() throws Exception {
		final String resourceType = UUID.randomUUID().toString();
		//when
		mockMvc.perform(adminGET("/rest-api/v1/sites/{siteId}/resourceTypes/{resourceTypeId}", site.getId().id,
				resourceType))
				.andDo(print())
				.andExpect(status().isNotFound());
	}

	@Test
	void shouldReturnNotFoundIfSiteDoesNotExistsWhileGettingResourceType() throws Exception {
		final String fakeSiteId = UUID.randomUUID().toString();
		final String resourceType = UUID.randomUUID().toString();
		//when
		mockMvc.perform(adminGET("/rest-api/v1/sites/{siteId}/resourceTypes/{resourceTypeId}", fakeSiteId, resourceType))
				.andDo(print())
				.andExpect(status().isNotFound());
	}

	private ResourceTypeId createResourceType(SiteId siteId, String name) {
		final PolicyId policyId = policyDocumentRepository.create(defaultPolicy()
				.siteId(siteId)
				.build());
		final InfraServiceId serviceId = infraServiceRepository.create(defaultService()
				.siteId(siteId)
				.name(UUID.randomUUID().toString())
				.policyId(policyId)
				.build());
		return resourceTypeRepository.create(defaultResourceType()
				.siteId(siteId)
				.serviceId(serviceId)
				.name(name)
				.build());
	}

}
