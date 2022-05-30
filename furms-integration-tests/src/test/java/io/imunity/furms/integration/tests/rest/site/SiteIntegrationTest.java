/*
 * Copyright (c) 2021 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.integration.tests.rest.site;

import io.imunity.furms.domain.communities.CommunityId;
import io.imunity.furms.domain.policy_documents.PolicyId;
import io.imunity.furms.domain.projects.ProjectId;
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
import static org.hamcrest.Matchers.in;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class SiteIntegrationTest extends IntegrationTestBase {

	private ProjectId projectId;
	private Site site;

	@BeforeEach
	void setUp() {
		Site.SiteBuilder siteBuilder = defaultSite().name("site1");
		site = siteBuilder
				.id(siteRepository.create(siteBuilder.build(), siteBuilder.build().getExternalId()))
				.build();
		final CommunityId communityId = communityRepository.create(defaultCommunity().build());
		projectId = projectRepository.create(defaultProject()
				.communityId(communityId)
				.build());
		final PolicyId policyId = policyDocumentRepository.create(defaultPolicy()
				.siteId(site.getId())
				.build());
		final InfraServiceId serviceId = infraServiceRepository.create(defaultService()
				.siteId(site.getId())
				.policyId(policyId)
				.build());
		final ResourceTypeId resourceTypeId = resourceTypeRepository.create(defaultResourceType()
				.siteId(site.getId())
				.serviceId(serviceId)
				.build());
		resourceCreditRepository.create(defaultResourceCredit()
				.siteId(site.getId())
				.resourceTypeId(resourceTypeId)
				.build());
	}

	@Test
	void shouldGetAllSitesThatCurrentUserIsInstalledOnTheseSites() throws Exception {
		//given
		final Site notMySite = defaultSite().name("NotMySite")
			.build();
		siteRepository.create(notMySite, new SiteExternalId("nmsid"));
		final TestUser testUser = basicUser();
		testUser.addSiteSupport(site.getId());
		setupUser(testUser);

		userOperationRepository.create(defaultUserAddition()
				.userId(testUser.getFenixId())
				.siteId(site.getId())
				.projectId(projectId)
				.build());

		//when
		mockMvc.perform(get("/rest-api/v1/sites/")
				.with(testUser.getHttpBasic()))
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
	void shouldGetAllSitesForNonCentralIdpUser() throws Exception {
		//given
		final TestUser testUser = basicUser();
		final Site siteWithAdmin = defaultSite().name("siteWithAdmin")
			.build();
		final SiteId siteWithAdminId = siteRepository.create(siteWithAdmin, new SiteExternalId("admid"));
		testUser.addSiteSupport(siteWithAdminId);
		final Site siteWithSupport = defaultSite().name("siteWithSupport")
			.build();
		final SiteId siteWithSupportId = siteRepository.create(siteWithSupport, new SiteExternalId("supid"));
		testUser.addSiteSupport(siteWithSupportId);

		testUser.disableCentralIDPIdentity(server);
		setupUser(testUser);

		//when
		mockMvc.perform(get("/rest-api/v1/sites/")
				.with(testUser.getHttpBasic()))
				.andDo(print())
				.andExpect(status().isOk())
				.andExpect(jsonPath("$", hasSize(2)))
				.andExpect(jsonPath("$.[0].id", not(site.getId())))
				.andExpect(jsonPath("$.[1].id", not(site.getId())));
	}

	@Test
	void shouldGetAllSitesForFenixAdmin() throws Exception {
		//given
		final Site extraSite = defaultSite().name("extraSite").build();
		final SiteId extraSiteId = siteRepository.create(extraSite, new SiteExternalId("admid"));
		final Site extraSite2 = defaultSite().name("extraSite2").build();
		final SiteId extraSite2Id = siteRepository.create(extraSite2, new SiteExternalId("supid"));

		final Set<String> expectedSites = Set.of(extraSiteId.id.toString(), extraSite2Id.id.toString(),
			site.getId().id.toString());

		//when
		mockMvc.perform(adminGET("/rest-api/v1/sites/"))
				.andDo(print())
				.andExpect(status().isOk())
				.andExpect(jsonPath("$", hasSize(3)))
				.andExpect(jsonPath("$.[0].id", in(expectedSites)))
				.andExpect(jsonPath("$.[1].id", in(expectedSites)))
				.andExpect(jsonPath("$.[2].id", in(expectedSites)));
	}

	@Test
	void shouldFindSiteBySiteIdThatBelongsToUser() throws Exception {
		//when
		mockMvc.perform(adminGET("/rest-api/v1/sites/{siteId}", site.getId().id))
				.andDo(print())
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.name", equalTo(site.getName())));
	}

	@Test
	void shouldGetUnauthorizedWhileUserNotBelongsToThisSite() throws Exception {
		//given
		final Site notMySite = defaultSite().name("NotMySite")
			.build();
		final SiteId notMySiteId2 = siteRepository.create(notMySite, new SiteExternalId("nmsid"));
		userOperationRepository.create(defaultUserAddition()
				.userId(ADMIN_USER.getFenixId())
				.siteId(site.getId())
				.projectId(projectId)
				.build());

		final TestUser testUser = basicUser();
		setupUser(testUser);

		//when
		mockMvc.perform(get("/rest-api/v1/sites/{siteId}", notMySiteId2.id)
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
