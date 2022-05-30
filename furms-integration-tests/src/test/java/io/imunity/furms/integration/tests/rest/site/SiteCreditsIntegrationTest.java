/*
 * Copyright (c) 2021 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.integration.tests.rest.site;

import io.imunity.furms.domain.policy_documents.PolicyId;
import io.imunity.furms.domain.resource_credits.ResourceCreditId;
import io.imunity.furms.domain.resource_types.ResourceTypeId;
import io.imunity.furms.domain.services.InfraServiceId;
import io.imunity.furms.domain.sites.Site;
import io.imunity.furms.domain.sites.SiteExternalId;
import io.imunity.furms.domain.sites.SiteId;
import io.imunity.furms.integration.tests.IntegrationTestBase;
import io.imunity.furms.integration.tests.tools.users.TestUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Set;
import java.util.UUID;

import static io.imunity.furms.integration.tests.tools.DefaultDataBuilders.defaultPolicy;
import static io.imunity.furms.integration.tests.tools.DefaultDataBuilders.defaultResourceCredit;
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

public class SiteCreditsIntegrationTest extends IntegrationTestBase {

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
		final ResourceCreditId resourceCredit1 = createResourceCredit(site.getId(), "Test 1", BigDecimal.valueOf(1));
		final ResourceCreditId resourceCredit2 = createResourceCredit(site.getId(),"Test 2", BigDecimal.valueOf(2));
		createResourceCredit(darkSite.getId(),"Test 3", BigDecimal.valueOf(3));

		//when
		mockMvc.perform(adminGET("/rest-api/v1/sites/{siteId}/credits", site.getId().id))
				.andDo(print())
				.andExpect(status().isOk())
				.andExpect(jsonPath("$", hasSize(2)))
				.andExpect(jsonPath("$.[0].creditId", in(Set.of(resourceCredit1.id.toString(), resourceCredit2.id.toString()))))
				.andExpect(jsonPath("$.[0].amount.amount", in(Set.of(1, 2))))
				.andExpect(jsonPath("$.[0].amount.unit", equalTo("GB")))
				.andExpect(jsonPath("$.[0].name", in(Set.of("Test 1", "Test 2"))))
				.andExpect(jsonPath("$.[0].resourceTypeId", notNullValue()))
				.andExpect(jsonPath("$.[0].validity", notNullValue()))
				.andExpect(jsonPath("$.[1].creditId", in(Set.of(resourceCredit1.id.toString(), resourceCredit2.id.toString()))))
				.andExpect(jsonPath("$.[1].amount.amount", in(Set.of(1, 2))))
				.andExpect(jsonPath("$.[1].amount.unit", equalTo("GB")))
				.andExpect(jsonPath("$.[1].name", in(Set.of("Test 1", "Test 2"))))
				.andExpect(jsonPath("$.[1].resourceTypeId", notNullValue()))
				.andExpect(jsonPath("$.[1].validity", notNullValue()));
	}

	@Test
	void shouldReturnNotFoundIfSiteDoesNotExistDuringGettingAllCredits() throws Exception {
		//when
		mockMvc.perform(adminGET("/rest-api/v1/sites/{siteId}/credits", UUID.randomUUID().toString()))
				.andDo(print())
				.andExpect(status().isNotFound());
	}

	@Test
	void shouldReturnForbiddenIfSiteDoesNotBelongToUserDuringGettingAllCredits() throws Exception {
		final TestUser testUser = basicUser();
		setupUser(testUser);

		//when
		mockMvc.perform(get("/rest-api/v1/sites/{siteId}/credits", site.getId().id)
				.with(testUser.getHttpBasic()))
				.andDo(print())
				.andExpect(status().isForbidden());
	}

	@Test
	void shouldFindResourceCreditByIdThatBelongsToSite() throws Exception {
		//given
		final ResourceCreditId resourceCredit1 = createResourceCredit(site.getId(), "Test 1", BigDecimal.valueOf(1));
		final ResourceCreditId resourceCredit2 = createResourceCredit(site.getId(),"Test 2", BigDecimal.valueOf(2));
		createResourceCredit(darkSite.getId(),"Test 3", BigDecimal.valueOf(3));

		//when
		mockMvc.perform(adminGET("/rest-api/v1/sites/{siteId}/credits/{creditId}", site.getId().id, resourceCredit1.id))
				.andDo(print())
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.creditId", in(Set.of(resourceCredit1.id.toString(), resourceCredit2.id.toString()))))
				.andExpect(jsonPath("$.amount.amount", in(Set.of(1, 2))))
				.andExpect(jsonPath("$.amount.unit", equalTo("GB")))
				.andExpect(jsonPath("$.name", in(Set.of("Test 1", "Test 2"))))
				.andExpect(jsonPath("$.resourceTypeId", notNullValue()))
				.andExpect(jsonPath("$.validity", notNullValue()));
	}

	@Test
	void shouldReturnForbiddenIfSiteDoesNotBelongToCredit() throws Exception {
		//given
		final ResourceCreditId resourceCreditId = createResourceCredit(darkSite.getId(), "Test 3", BigDecimal.valueOf(3));

		final TestUser testUser = basicUser();
		testUser.addSiteAdmin(site.getId());
		setupUser(testUser);

		//when
		mockMvc.perform(get("/rest-api/v1/sites/{siteId}/credits/{creditId}", site.getId().id, resourceCreditId.id)
				.with(testUser.getHttpBasic()))
				.andDo(print())
				.andExpect(status().isForbidden());
	}

	@Test
	void shouldReturnNotFoundIfCreditDoesNotExists() throws Exception {
		//given
		final String resourceCreditFakeId = UUID.randomUUID().toString();

		final TestUser testUser = basicUser();
		testUser.addSiteAdmin(site.getId());
		setupUser(testUser);

		//when
		mockMvc.perform(get("/rest-api/v1/sites/{siteId}/credits/{creditId}", site.getId().id, resourceCreditFakeId)
				.with(testUser.getHttpBasic()))
				.andDo(print())
				.andExpect(status().isNotFound());
	}

	private ResourceCreditId createResourceCredit(SiteId siteId, String name, BigDecimal amount) {
		final PolicyId policyId = policyDocumentRepository.create(defaultPolicy()
				.siteId(siteId)
				.build());
		final InfraServiceId serviceId = infraServiceRepository.create(defaultService()
				.siteId(siteId)
				.name(UUID.randomUUID().toString())
				.policyId(policyId)
				.build());
		final ResourceTypeId resourceTypeId = resourceTypeRepository.create(defaultResourceType()
				.siteId(siteId)
				.serviceId(serviceId)
				.name(UUID.randomUUID().toString())
				.build());
		return resourceCreditRepository.create(defaultResourceCredit()
				.siteId(siteId)
				.resourceTypeId(resourceTypeId)
				.name(name)
				.amount(amount)
				.build());
	}
}
