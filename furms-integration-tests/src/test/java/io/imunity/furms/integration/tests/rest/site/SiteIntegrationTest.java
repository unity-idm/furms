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

import java.util.UUID;

import static io.imunity.furms.integration.tests.tools.DefaultDataBuilders.defaultCommunity;
import static io.imunity.furms.integration.tests.tools.DefaultDataBuilders.defaultPolicy;
import static io.imunity.furms.integration.tests.tools.DefaultDataBuilders.defaultProject;
import static io.imunity.furms.integration.tests.tools.DefaultDataBuilders.defaultResourceCredit;
import static io.imunity.furms.integration.tests.tools.DefaultDataBuilders.defaultResourceType;
import static io.imunity.furms.integration.tests.tools.DefaultDataBuilders.defaultService;
import static io.imunity.furms.integration.tests.tools.DefaultDataBuilders.defaultSite;
import static io.imunity.furms.integration.tests.tools.DefaultDataBuilders.defaultUserAddition;
import static io.imunity.furms.integration.tests.tools.users.TestUsersProvider.basicUser;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class SiteIntegrationTest extends IntegrationTestBase {

	private String projectId;
	private Site site;

	@BeforeEach
	void setUp() {
		Site.SiteBuilder siteBuilder = defaultSite().name("site1");
		site = siteBuilder
				.id(siteRepository.create(siteBuilder.build(), siteBuilder.build().getExternalId()))
				.build();
		final String communityId = communityRepository.create(defaultCommunity().build());
		projectId = projectRepository.create(defaultProject()
				.communityId(communityId)
				.build());
		final PolicyId policyId = policyDocumentRepository.create(defaultPolicy()
				.siteId(site.getId())
				.build());
		final String serviceId = infraServiceRepository.create(defaultService()
				.siteId(site.getId())
				.policyId(policyId)
				.build());
		final String resourceTypeId = resourceTypeRepository.create(defaultResourceType()
				.siteId(site.getId())
				.serviceId(serviceId)
				.build());
		resourceCreditRepository.create(defaultResourceCredit()
				.siteId(site.getId())
				.resourceTypeId(resourceTypeId)
				.build());
	}

	@Test
	void shouldGetAllSitesThatRelatedToCurrentUser() throws Exception {
		//given
		final Site notMySite = defaultSite().name("NotMySite").externalId(new SiteExternalId("nmsid")).build();
		final String notMySiteId2 = siteRepository.create(notMySite, notMySite.getExternalId());
		userOperationRepository.create(defaultUserAddition()
				.userId(ADMIN_USER.getFenixId())
				.siteId(new SiteId(site.getId()))
				.projectId(projectId)
				.build());

		//when
		mockMvc.perform(adminGET("/rest-api/v1/sites/"))
				.andDo(print())
				.andExpect(status().isOk())
				.andExpect(jsonPath("$", hasSize(1)))
				.andExpect(jsonPath("$.[0].name", equalTo(site.getName())))
				.andExpect(jsonPath("$.[0].resourceCredits", hasSize(1)))
				.andExpect(jsonPath("$.[0].resourceTypes", hasSize(1)))
				.andExpect(jsonPath("$.[0].services", hasSize(1)))
				.andExpect(jsonPath("$.[0].policies", hasSize(1)));
	}

	@Test
	void shouldFindSiteBySiteIdThatBelongsToUser() throws Exception {
		//when
		mockMvc.perform(adminGET("/rest-api/v1/sites/{siteId}", site.getId()))
				.andDo(print())
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.name", equalTo(site.getName())));
	}

	@Test
	void shouldGetUnauthorizedWhileUserNotBelongsToThisSite() throws Exception {
		//given
		final Site notMySite = defaultSite().name("NotMySite").externalId(new SiteExternalId("nmsid")).build();
		final String notMySiteId2 = siteRepository.create(notMySite, notMySite.getExternalId());
		userOperationRepository.create(defaultUserAddition()
				.userId(ADMIN_USER.getFenixId())
				.siteId(new SiteId(site.getId()))
				.projectId(projectId)
				.build());

		final TestUser testUser = basicUser();
		setupUser(testUser);

		//when
		mockMvc.perform(get("/rest-api/v1/sites/{siteId}", notMySiteId2)
				.with(testUser.getHttpBasic()))
				.andDo(print())
				.andExpect(status().isForbidden());
	}

	@Test
	void shouldGetNotFoundWhenSiteDoesNotExists() throws Exception {
		mockMvc.perform(adminGET("/rest-api/v1/sites/{siteId}", UUID.randomUUID().toString()))
				.andDo(print())
				.andExpect(status().isNotFound());
	}

}
