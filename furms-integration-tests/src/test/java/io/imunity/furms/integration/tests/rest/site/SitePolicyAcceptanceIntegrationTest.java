/*
 * Copyright (c) 2021 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.integration.tests.rest.site;

import com.github.tomakehurst.wiremock.client.WireMock;
import io.imunity.furms.core.user_operation.UserOperationService;
import io.imunity.furms.domain.policy_documents.PolicyId;
import io.imunity.furms.domain.resource_access.GrantAccess;
import io.imunity.furms.domain.site_agent.CorrelationId;
import io.imunity.furms.domain.sites.Site;
import io.imunity.furms.domain.sites.SiteExternalId;
import io.imunity.furms.domain.sites.SiteId;
import io.imunity.furms.domain.users.FenixUserId;
import io.imunity.furms.integration.tests.IntegrationTestBase;
import io.imunity.furms.integration.tests.tools.PolicyAcceptanceMockUtils;
import io.imunity.furms.integration.tests.tools.PolicyAcceptanceMockUtils.PolicyUser;
import io.imunity.furms.integration.tests.tools.users.TestUser;
import io.imunity.furms.site.api.site_agent.SiteAgentPolicyDocumentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static io.imunity.furms.domain.resource_access.AccessStatus.GRANT_PENDING;
import static io.imunity.furms.integration.tests.tools.DefaultDataBuilders.defaultCommunity;
import static io.imunity.furms.integration.tests.tools.DefaultDataBuilders.defaultCommunityAllocation;
import static io.imunity.furms.integration.tests.tools.DefaultDataBuilders.defaultPolicy;
import static io.imunity.furms.integration.tests.tools.DefaultDataBuilders.defaultProject;
import static io.imunity.furms.integration.tests.tools.DefaultDataBuilders.defaultProjectAllocation;
import static io.imunity.furms.integration.tests.tools.DefaultDataBuilders.defaultResourceCredit;
import static io.imunity.furms.integration.tests.tools.DefaultDataBuilders.defaultResourceType;
import static io.imunity.furms.integration.tests.tools.DefaultDataBuilders.defaultService;
import static io.imunity.furms.integration.tests.tools.DefaultDataBuilders.defaultSite;
import static io.imunity.furms.integration.tests.tools.users.TestUsersProvider.basicUser;
import static io.imunity.furms.rest.admin.AcceptanceStatus.ACCEPTED;
import static io.imunity.furms.rest.admin.AcceptanceStatus.ACCEPTED_FORMER_REVISION;
import static io.imunity.furms.rest.admin.AcceptanceStatus.NOT_ACCEPTED;
import static org.apache.http.HttpHeaders.CONTENT_TYPE;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.in;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.util.MimeTypeUtils.APPLICATION_JSON_VALUE;

public class SitePolicyAcceptanceIntegrationTest extends IntegrationTestBase {

	private Site site;
	private Site darkSite;

	private PolicyAcceptanceMockUtils policyAcceptanceMockUtils;

	@MockBean
	private SiteAgentPolicyDocumentService siteAgentPolicyDocumentService;
	@MockBean
	private UserOperationService userOperationService;

	@BeforeEach
	void setUp() {
		policyAcceptanceMockUtils = new PolicyAcceptanceMockUtils(objectMapper, server);
		Site.SiteBuilder siteBuilder = defaultSite();
		site = siteBuilder
				.name("Site")
				.externalId(new SiteExternalId("site"))
				.id(siteRepository.create(siteBuilder.build(), siteBuilder.build().getExternalId()))
				.policyId(assignPolicy(siteBuilder))
				.build();

		Site.SiteBuilder darkSiteBuilder = defaultSite();
		darkSite = darkSiteBuilder
				.name("Dark Site")
				.externalId(new SiteExternalId("dsid"))
				.id(siteRepository.create(darkSiteBuilder.build(), darkSiteBuilder.build().getExternalId()))
				.policyId(assignPolicy(darkSiteBuilder))
				.build();
	}

	@Test
	void shouldFindAllAcceptedPolicyAcceptancesForSpecificSiteAndEmptyServicePolicy() throws Exception {
		//given
		final TestUser basicUser = basicUser();
		final String sitePath = createPolicyAcceptanceBase(site, ADMIN_USER, PolicyId.empty());
		createPolicyAcceptanceBase(site, basicUser);
		final String darkSitePath = createPolicyAcceptanceBase(darkSite, ADMIN_USER, PolicyId.empty());

		policyAcceptanceMockUtils.createPolicyAcceptancesMock(sitePath, List.of(
				new PolicyUser(site.getPolicyId().id.toString(), ADMIN_USER),
				new PolicyUser(site.getPolicyId().id.toString(), basicUser)));
		policyAcceptanceMockUtils.createPolicyAcceptancesMock(darkSitePath, List.of(
				new PolicyUser(darkSite.getPolicyId().id.toString(), ADMIN_USER)));

		//when
		mockMvc.perform(adminGET("/rest-api/v1/sites/{siteId}/policyAcceptances", site.getId()))
				.andDo(print())
				.andExpect(status().isOk())
				.andExpect(jsonPath("$", hasSize(2)))
				.andExpect(jsonPath("$.[0].fenixUserId", in(Set.of(ADMIN_USER.getFenixId(), basicUser.getFenixId()))))
				.andExpect(jsonPath("$.[0].policyId", equalTo(site.getPolicyId().id.toString())))
				.andExpect(jsonPath("$.[0].acceptedRevision", equalTo(1)))
				.andExpect(jsonPath("$.[0].currentPolicyRevision", equalTo(1)))
				.andExpect(jsonPath("$.[0].acceptanceStatus", equalTo(ACCEPTED.name())))
				.andExpect(jsonPath("$.[1].fenixUserId", in(Set.of(ADMIN_USER.getFenixId(), basicUser.getFenixId()))))
				.andExpect(jsonPath("$.[1].policyId", equalTo(site.getPolicyId().id.toString())))
				.andExpect(jsonPath("$.[1].acceptanceStatus", equalTo(ACCEPTED.name())))
				.andExpect(jsonPath("$.[1].acceptedRevision", equalTo(1)))
				.andExpect(jsonPath("$.[1].currentPolicyRevision", equalTo(1)));
	}

	@Test
	void shouldFindAllAcceptedFormerRevisionPolicyAcceptancesForSpecificSite() throws Exception {
		//given
		final TestUser basicUser = basicUser();
		createPolicy(site.getId());
		final String sitePath = createPolicyAcceptanceBase(site, ADMIN_USER);
		createPolicyAcceptanceBase(site, basicUser);
		final String darkSitePath = createPolicyAcceptanceBase(darkSite, ADMIN_USER);

		policyAcceptanceMockUtils.createPolicyAcceptancesMock(sitePath, List.of(
			new PolicyUser(site.getPolicyId().id.toString(), ADMIN_USER),
			new PolicyUser(site.getPolicyId().id.toString(), basicUser)));
		policyAcceptanceMockUtils.createPolicyAcceptancesMock(darkSitePath, List.of(
			new PolicyUser(darkSite.getPolicyId().id.toString(), ADMIN_USER)));

		updatePolicyRevision(site.getPolicyId());

		//when
		mockMvc.perform(adminGET("/rest-api/v1/sites/{siteId}/policyAcceptances", site.getId()))
			.andDo(print())
			.andExpect(status().isOk())
			.andExpect(jsonPath("$", hasSize(2)))
			.andExpect(jsonPath("$.[0].fenixUserId", in(Set.of(ADMIN_USER.getFenixId(), basicUser.getFenixId()))))
			.andExpect(jsonPath("$.[0].policyId", equalTo(site.getPolicyId().id.toString())))
			.andExpect(jsonPath("$.[0].acceptedRevision", equalTo(1)))
			.andExpect(jsonPath("$.[0].currentPolicyRevision", equalTo(2)))
			.andExpect(jsonPath("$.[0].acceptanceStatus", equalTo(ACCEPTED_FORMER_REVISION.name())))
			.andExpect(jsonPath("$.[1].fenixUserId", in(Set.of(ADMIN_USER.getFenixId(), basicUser.getFenixId()))))
			.andExpect(jsonPath("$.[1].policyId", equalTo(site.getPolicyId().id.toString())))
			.andExpect(jsonPath("$.[1].acceptanceStatus", equalTo(ACCEPTED_FORMER_REVISION.name())))
			.andExpect(jsonPath("$.[1].acceptedRevision", equalTo(1)))
			.andExpect(jsonPath("$.[1].currentPolicyRevision", equalTo(2)));
	}

	private void updatePolicyRevision(PolicyId policyId) {
		policyDocumentRepository.update(
			policyDocumentRepository.findById(policyId).get(),
			true
		);
	}

	@Test
	void shouldFindAllNotAcceptedPolicyAcceptancesForSpecificSite() throws Exception {
		//given
		final TestUser basicUser = basicUser();
		PolicyId policyId = createPolicy(site.getId());
		final String sitePath = createPolicyAcceptanceBase(site, ADMIN_USER, policyId);
		createPolicyAcceptanceBase(site, basicUser, policyId);

		PolicyId darkPolicyId = createPolicy(site.getId());
		final String darkSitePath = createPolicyAcceptanceBase(darkSite, ADMIN_USER, darkPolicyId);

		policyAcceptanceMockUtils.createPolicyAcceptancesMock(sitePath, List.of());
		policyAcceptanceMockUtils.createPolicyAcceptancesMock(darkSitePath, List.of(
			new PolicyUser(darkSite.getPolicyId().id.toString(), ADMIN_USER)));

		//when
		mockMvc.perform(adminGET("/rest-api/v1/sites/{siteId}/policyAcceptances", site.getId()))
			.andDo(print())
			.andExpect(status().isOk())
			.andExpect(jsonPath("$", hasSize(4)))
			.andExpect(jsonPath("$.[0].fenixUserId", in(Set.of(ADMIN_USER.getFenixId(), basicUser.getFenixId()))))
			.andExpect(jsonPath("$.[0].policyId", in(Set.of(site.getPolicyId().id.toString(), policyId.id.toString()))))
			.andExpect(jsonPath("$.[0].acceptedRevision", equalTo(null)))
			.andExpect(jsonPath("$.[0].currentPolicyRevision", equalTo(1)))
			.andExpect(jsonPath("$.[0].acceptanceStatus", equalTo(NOT_ACCEPTED.name())))
			.andExpect(jsonPath("$.[1].fenixUserId", in(Set.of(ADMIN_USER.getFenixId(), basicUser.getFenixId()))))
			.andExpect(jsonPath("$.[1].policyId", in(Set.of(site.getPolicyId().id.toString(), policyId.id.toString()))))
			.andExpect(jsonPath("$.[1].acceptanceStatus", equalTo(NOT_ACCEPTED.name())))
			.andExpect(jsonPath("$.[1].acceptedRevision", equalTo(null)))
			.andExpect(jsonPath("$.[1].currentPolicyRevision", equalTo(1)))
			.andExpect(jsonPath("$.[2].fenixUserId", in(Set.of(ADMIN_USER.getFenixId(), basicUser.getFenixId()))))
			.andExpect(jsonPath("$.[2].policyId", in(Set.of(site.getPolicyId().id.toString(), policyId.id.toString()))))
			.andExpect(jsonPath("$.[2].acceptedRevision", equalTo(null)))
			.andExpect(jsonPath("$.[2].currentPolicyRevision", equalTo(1)))
			.andExpect(jsonPath("$.[2].acceptanceStatus", equalTo(NOT_ACCEPTED.name())))
			.andExpect(jsonPath("$.[3].fenixUserId", in(Set.of(ADMIN_USER.getFenixId(), basicUser.getFenixId()))))
			.andExpect(jsonPath("$.[3].policyId", in(Set.of(site.getPolicyId().id.toString(), policyId.id.toString()))))
			.andExpect(jsonPath("$.[3].acceptanceStatus", equalTo(NOT_ACCEPTED.name())))
			.andExpect(jsonPath("$.[3].acceptedRevision", equalTo(null)))
			.andExpect(jsonPath("$.[3].currentPolicyRevision", equalTo(1)));
	}

	@Test
	void shouldReturnNotFoundIfSiteDoesNotExistsWhileGettingPolicyAcceptances() throws Exception {
		//when
		mockMvc.perform(adminGET("/rest-api/v1/sites/{siteId}/policyAcceptances", UUID.randomUUID().toString()))
				.andDo(print())
				.andExpect(status().isNotFound());
	}

	@Test
	void shouldReturnEmptyArrayWhenThereAreNoBelongsPolicyAcceptances() throws Exception {
		//when
		mockMvc.perform(adminGET("/rest-api/v1/sites/{siteId}/policyAcceptances", site.getId()))
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
		mockMvc.perform(get("/rest-api/v1/sites/{siteId}/policyAcceptances", darkSite.getId())
				.with(testUser.getHttpBasic()))
				.andDo(print())
				.andExpect(status().isForbidden());
	}

	@Test
	void shouldAcceptPolicyAcceptance() throws Exception {
		//given
		server.stubFor(WireMock.put("/unity/entity/"+ADMIN_USER.getFenixId()+"/attribute?identityType=identifier")
				.willReturn(aResponse().withStatus(200)
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)));

		policyAcceptanceMockUtils.createPolicyAcceptancesMock(createPolicyAcceptanceBase(site, ADMIN_USER),
				List.of(new PolicyUser(site.getPolicyId().id.toString(), ADMIN_USER)));

		//when
		mockMvc.perform(adminPOST("/rest-api/v1/sites/{siteId}/policies/{policyId}/acceptance/{fenixUserId}",
					site.getId(), site.getPolicyId().id, ADMIN_USER.getFenixId()))
				.andDo(print())
				.andExpect(status().isOk());
	}

	@Test
	void shouldNotAllowToAddPolicyAcceptanceDueToLackOfPermissions() throws Exception {
		//given
		final TestUser testUser = basicUser();
		setupUser(testUser);

		//when
		mockMvc.perform(post("/rest-api/v1/sites/{siteId}/policies/{policyId}/acceptance/{fenixUserId}",
					site.getId(), site.getPolicyId().id, ADMIN_USER.getFenixId())
				.with(basicUser().getHttpBasic()))
				.andDo(print())
				.andExpect(status().isForbidden());
	}

	@Test
	void shouldNotAllowToAddPolicyAcceptanceWhenPolicyDoesNotExists() throws Exception {
		//when
		mockMvc.perform(adminPOST("/rest-api/v1/sites/{siteId}/policies/{policyId}/acceptance/{fenixUserId}",
				site.getId(), UUID.randomUUID().toString(), ADMIN_USER.getFenixId()))
				.andDo(print())
				.andExpect(status().isBadRequest());
	}

	@Test
	void shouldNotAllowToAddPolicyAcceptanceWhenSiteDoesNotExists() throws Exception {
		//given
		final TestUser testUser = basicUser();
		testUser.addSiteAdmin(site.getId());
		setupUser(testUser);

		//when
		mockMvc.perform(post("/rest-api/v1/sites/{siteId}/policies/{policyId}/acceptance/{fenixUserId}",
				UUID.randomUUID().toString(), site.getPolicyId().id, ADMIN_USER.getFenixId())
				.with(testUser.getHttpBasic()))
				.andDo(print())
				.andExpect(status().isForbidden());

		//when
		server.stubFor(WireMock.put("/unity/entity/"+ADMIN_USER.getFenixId()+"/attribute?identityType=identifier")
				.willReturn(aResponse().withStatus(200)
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)));

		mockMvc.perform(adminPOST("/rest-api/v1/sites/{siteId}/policies/{policyId}/acceptance/{fenixUserId}",
				UUID.randomUUID().toString(), site.getPolicyId().id, ADMIN_USER.getFenixId()))
				.andDo(print())
				.andExpect(status().isBadRequest());
	}

	private PolicyId assignPolicy(Site.SiteBuilder siteBuilder) {
		final PolicyId policyId = createPolicy(siteBuilder.build().getId());
		siteRepository.update(siteBuilder
				.policyId(policyId)
				.build());
		return policyId;
	}

	private PolicyId createPolicy(String siteId) {
		return policyDocumentRepository.create(defaultPolicy()
			.siteId(siteId)
			.name(UUID.randomUUID().toString())
			.build());
	}

	private String createPolicyAcceptanceBase(Site site, TestUser testUser) {
		return createPolicyAcceptanceBase(site, testUser, site.getPolicyId());
	}

	private String createPolicyAcceptanceBase(Site site, TestUser testUser, PolicyId servicePolicyId) {
		final String communityId = communityRepository.create(defaultCommunity()
				.name(UUID.randomUUID().toString())
				.build());
		final String projectId = projectRepository.create(defaultProject()
				.communityId(communityId)
				.name(UUID.randomUUID().toString())
				.build());
		final String serviceId = infraServiceRepository.create(defaultService()
				.siteId(site.getId())
				.policyId(servicePolicyId)
				.name(UUID.randomUUID().toString())
				.build());
		final String resourceTypeId = resourceTypeRepository.create(defaultResourceType()
				.siteId(site.getId())
				.serviceId(serviceId)
				.name(UUID.randomUUID().toString())
				.build());
		final String resourceCreditId = resourceCreditRepository.create(defaultResourceCredit()
				.siteId(site.getId())
				.resourceTypeId(resourceTypeId)
				.amount(BigDecimal.TEN)
				.name(UUID.randomUUID().toString())
				.build());
		final String communityAllocationId = communityAllocationRepository.create(defaultCommunityAllocation()
				.resourceCreditId(resourceCreditId)
				.communityId(communityId)
				.amount(BigDecimal.valueOf(5))
				.name(UUID.randomUUID().toString())
				.build());
		String projectAllocationId = projectAllocationRepository.create(defaultProjectAllocation()
			.communityAllocationId(communityAllocationId)
			.projectId(projectId)
			.amount(BigDecimal.ONE)
			.name(UUID.randomUUID().toString())
			.build());
		GrantAccess grantAccess = GrantAccess.builder()
			.siteId(new SiteId(site.getId(), "mock"))
			.allocationId(projectAllocationId)
			.projectId(projectId)
			.fenixUserId(new FenixUserId(testUser.getFenixId()))
			.build();

		resourceAccessDatabaseRepository.create(CorrelationId.randomID(), grantAccess, GRANT_PENDING);
		return "/fenix/communities/"+communityId+"/projects/"+projectId+"/users";
	}

}
