/*
 * Copyright (c) 2021 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.integration.tests.rest.site;

import io.imunity.furms.domain.policy_documents.PolicyId;
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
import static io.imunity.furms.integration.tests.tools.DefaultDataBuilders.defaultSite;
import static io.imunity.furms.integration.tests.tools.users.TestUsersProvider.basicUser;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.in;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class SitePolicyIntegrationTest extends IntegrationTestBase {

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
	void shouldFindAllPoliciesForSpecificSite() throws Exception {
		//given
		final PolicyId policy1 = createPolicy(site.getId(), "Test 1", 1);
		final PolicyId policy2 = createPolicy(site.getId(),"Test 2", 1);
		createPolicy(darkSite.getId(),"Test 3", 1);

		//when
		mockMvc.perform(adminGET("/rest-api/v1/sites/{siteId}/policies", site.getId().id))
				.andDo(print())
				.andExpect(status().isOk())
				.andExpect(jsonPath("$", hasSize(2)))
				.andExpect(jsonPath("$.[0].policyId", in(Set.of(policy1.id.toString(), policy2.id.toString()))))
				.andExpect(jsonPath("$.[0].name", in(Set.of("Test 1", "Test 2"))))
				.andExpect(jsonPath("$.[0].revision", equalTo(1)))
				.andExpect(jsonPath("$.[1].policyId", in(Set.of(policy1.id.toString(), policy2.id.toString()))))
				.andExpect(jsonPath("$.[1].name", in(Set.of("Test 1", "Test 2"))))
				.andExpect(jsonPath("$.[1].revision", equalTo(1)));
	}

	@Test
	void shouldReturnNotFoundIfSiteDoesNotExistsWhileGettingPolicies() throws Exception {
		//when
		mockMvc.perform(adminGET("/rest-api/v1/sites/{siteId}/policies", UUID.randomUUID().toString()))
				.andDo(print())
				.andExpect(status().isNotFound());
	}

	@Test
	void shouldReturnEmptyArrayWhenThereAreNoBelongsPolicies() throws Exception {
		//when
		mockMvc.perform(adminGET("/rest-api/v1/sites/{siteId}/policies", site.getId().id))
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
		mockMvc.perform(get("/rest-api/v1/sites/{siteId}/policies", darkSite.getId().id)
				.with(testUser.getHttpBasic()))
				.andDo(print())
				.andExpect(status().isForbidden());
	}

	@Test
	void shouldFindPolicyThatBelongsToSite() throws Exception {
		//given
		final PolicyId policyId = createPolicy(site.getId(), "Test 1", 1);
		createPolicy(darkSite.getId(),"Test 2", 1);

		//when
		mockMvc.perform(adminGET("/rest-api/v1/sites/{siteId}/policies/{policyId}", site.getId().id, policyId.id))
				.andDo(print())
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.policyId", equalTo(policyId.id.toString())))
				.andExpect(jsonPath("$.name", equalTo("Test 1")))
				.andExpect(jsonPath("$.revision", equalTo(1)));
	}

	@Test
	void shouldReturnForbiddenIfPolicyNotBelongsToSite() throws Exception {
		//given
		final PolicyId policy = createPolicy(site.getId(), "Test 1", 1);
		createPolicy(darkSite.getId(),"Test 2", 1);

		//when
		mockMvc.perform(adminGET("/rest-api/v1/sites/{siteId}/policies/{policyId}", darkSite.getId().id, policy.id.toString()))
				.andDo(print())
				.andExpect(status().isForbidden());
	}

	@Test
	void shouldReturnNotFoundIfPolicyDoesNotExistsWhileGettingPolicies() throws Exception {
		final String policy = UUID.randomUUID().toString();
		//when
		mockMvc.perform(adminGET("/rest-api/v1/sites/{siteId}/policies/{policyId}", site.getId().id, policy))
				.andDo(print())
				.andExpect(status().isNotFound());
	}

	@Test
	void shouldReturnNotFoundIfSiteDoesNotExistsWhileGettingPolicy() throws Exception {
		final PolicyId policy = createPolicy(site.getId(), "Test 1", 1);
		//when
		mockMvc.perform(adminGET("/rest-api/v1/sites/{siteId}/policies/{policyId}", UUID.randomUUID().toString(), policy))
				.andDo(print())
				.andExpect(status().isNotFound());
	}

	private PolicyId createPolicy(SiteId siteId, String name, int revision) {
		return policyDocumentRepository.create(defaultPolicy()
				.siteId(siteId)
				.name(name)
				.revision(revision)
				.build());
	}

}
