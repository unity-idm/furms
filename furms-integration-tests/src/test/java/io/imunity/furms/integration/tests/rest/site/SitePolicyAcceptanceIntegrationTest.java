/*
 * Copyright (c) 2021 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.integration.tests.rest.site;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.matching.ContainsPattern;
import io.imunity.furms.domain.policy_documents.PolicyId;
import io.imunity.furms.domain.resource_access.GrantAccess;
import io.imunity.furms.domain.site_agent.CorrelationId;
import io.imunity.furms.domain.sites.Site;
import io.imunity.furms.domain.sites.SiteExternalId;
import io.imunity.furms.domain.sites.SiteId;
import io.imunity.furms.domain.users.FenixUserId;
import io.imunity.furms.integration.tests.IntegrationTestBase;
import io.imunity.furms.integration.tests.tools.users.TestUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pl.edu.icm.unity.types.basic.Attribute;
import pl.edu.icm.unity.types.basic.AttributeExt;
import pl.edu.icm.unity.types.basic.MultiGroupMembers;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
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
import static io.imunity.furms.unity.common.UnityConst.FURMS_POLICY_ACCEPTANCE_STATE_ATTRIBUTE;
import static io.imunity.furms.unity.common.UnityConst.STRING;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;
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

	@BeforeEach
	void setUp() {
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

		createPolicyAcceptancesMock(sitePath, List.of(
				new PolicyUser(site.getPolicyId().id.toString(), ADMIN_USER),
				new PolicyUser(site.getPolicyId().id.toString(), basicUser)));
		createPolicyAcceptancesMock(darkSitePath, List.of(
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
				.andExpect(jsonPath("$.[0].accepted", equalTo(ACCEPTED.name())))
				.andExpect(jsonPath("$.[1].fenixUserId", in(Set.of(ADMIN_USER.getFenixId(), basicUser.getFenixId()))))
				.andExpect(jsonPath("$.[1].policyId", equalTo(site.getPolicyId().id.toString())))
				.andExpect(jsonPath("$.[1].accepted", equalTo(ACCEPTED.name())))
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

		createPolicyAcceptancesMock(sitePath, List.of(
			new PolicyUser(site.getPolicyId().id.toString(), ADMIN_USER),
			new PolicyUser(site.getPolicyId().id.toString(), basicUser)));
		createPolicyAcceptancesMock(darkSitePath, List.of(
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
			.andExpect(jsonPath("$.[0].accepted", equalTo(ACCEPTED_FORMER_REVISION.name())))
			.andExpect(jsonPath("$.[1].fenixUserId", in(Set.of(ADMIN_USER.getFenixId(), basicUser.getFenixId()))))
			.andExpect(jsonPath("$.[1].policyId", equalTo(site.getPolicyId().id.toString())))
			.andExpect(jsonPath("$.[1].accepted", equalTo(ACCEPTED_FORMER_REVISION.name())))
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

		createPolicyAcceptancesMock(sitePath, List.of());
		createPolicyAcceptancesMock(darkSitePath, List.of(
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
			.andExpect(jsonPath("$.[0].accepted", equalTo(NOT_ACCEPTED.name())))
			.andExpect(jsonPath("$.[1].fenixUserId", in(Set.of(ADMIN_USER.getFenixId(), basicUser.getFenixId()))))
			.andExpect(jsonPath("$.[1].policyId", in(Set.of(site.getPolicyId().id.toString(), policyId.id.toString()))))
			.andExpect(jsonPath("$.[1].accepted", equalTo(NOT_ACCEPTED.name())))
			.andExpect(jsonPath("$.[1].acceptedRevision", equalTo(null)))
			.andExpect(jsonPath("$.[1].currentPolicyRevision", equalTo(1)))
			.andExpect(jsonPath("$.[2].fenixUserId", in(Set.of(ADMIN_USER.getFenixId(), basicUser.getFenixId()))))
			.andExpect(jsonPath("$.[2].policyId", in(Set.of(site.getPolicyId().id.toString(), policyId.id.toString()))))
			.andExpect(jsonPath("$.[2].acceptedRevision", equalTo(null)))
			.andExpect(jsonPath("$.[2].currentPolicyRevision", equalTo(1)))
			.andExpect(jsonPath("$.[2].accepted", equalTo(NOT_ACCEPTED.name())))
			.andExpect(jsonPath("$.[3].fenixUserId", in(Set.of(ADMIN_USER.getFenixId(), basicUser.getFenixId()))))
			.andExpect(jsonPath("$.[3].policyId", in(Set.of(site.getPolicyId().id.toString(), policyId.id.toString()))))
			.andExpect(jsonPath("$.[3].accepted", equalTo(NOT_ACCEPTED.name())))
			.andExpect(jsonPath("$.[3].acceptedRevision", equalTo(null)))
			.andExpect(jsonPath("$.[3].currentPolicyRevision", equalTo(1)));
	}

	@Test
	void shouldReturnNotFoundIfSiteDoesNotExistsOrThereAreNoBelongsPolicies() throws Exception {
		mockMvc.perform(adminGET("/rest-api/v1/sites/{siteId}/policyAcceptances", UUID.randomUUID().toString()))
				.andDo(print())
				.andExpect(status().isNotFound());

		mockMvc.perform(adminGET("/rest-api/v1/sites/{siteId}/policyAcceptances", site.getId()))
				.andDo(print())
				.andExpect(status().isNotFound());
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
	void shouldAddPolicyAcceptance() throws Exception {
		//given
		server.stubFor(WireMock.put("/unity/entity/"+ADMIN_USER.getFenixId()+"/attribute?identityType=identifier")
				.willReturn(aResponse().withStatus(200)
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)));

		createPolicyAcceptancesMock(createPolicyAcceptanceBase(site, ADMIN_USER),
				List.of(new PolicyUser(site.getPolicyId().id.toString(), ADMIN_USER)));

		//when
		mockMvc.perform(adminPOST("/rest-api/v1/sites/{siteId}/policies/{policyId}/acceptance/{fenixUserId}/{status}",
					site.getId(), site.getPolicyId().id, ADMIN_USER.getFenixId(), ACCEPTED.name()))
				.andDo(print())
				.andExpect(status().isOk());
	}

	@Test
	void shouldNotAllowToAddPolicyAcceptanceDueToLackOfPermissions() throws Exception {
		//given
		final TestUser testUser = basicUser();
		setupUser(testUser);

		//when
		mockMvc.perform(post("/rest-api/v1/sites/{siteId}/policies/{policyId}/acceptance/{fenixUserId}/{status}",
					site.getId(), site.getPolicyId().id, ADMIN_USER.getFenixId(), ACCEPTED.name())
				.with(basicUser().getHttpBasic()))
				.andDo(print())
				.andExpect(status().isUnauthorized());
	}

	@Test
	void shouldNotAllowToAddPolicyAcceptanceWhenPolicyDoesNotExists() throws Exception {
		//when
		mockMvc.perform(adminPOST("/rest-api/v1/sites/{siteId}/policies/{policyId}/acceptance/{fenixUserId}/{status}",
				site.getId(), UUID.randomUUID().toString(), ADMIN_USER.getFenixId(), ACCEPTED.name()))
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
		mockMvc.perform(post("/rest-api/v1/sites/{siteId}/policies/{policyId}/acceptance/{fenixUserId}/{status}",
				UUID.randomUUID().toString(), site.getPolicyId().id, ADMIN_USER.getFenixId(), ACCEPTED.name())
				.with(testUser.getHttpBasic()))
				.andDo(print())
				.andExpect(status().isForbidden());

		//when
		server.stubFor(WireMock.put("/unity/entity/"+ADMIN_USER.getFenixId()+"/attribute?identityType=identifier")
				.willReturn(aResponse().withStatus(200)
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)));

		mockMvc.perform(adminPOST("/rest-api/v1/sites/{siteId}/policies/{policyId}/acceptance/{fenixUserId}/{status}",
				UUID.randomUUID().toString(), site.getPolicyId().id, ADMIN_USER.getFenixId(), ACCEPTED.name()))
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

	private void createPolicyAcceptancesMock(String path, List<PolicyUser> policies) throws JsonProcessingException {
		final MultiGroupMembers multiGroupMembers = new MultiGroupMembers(
				policies.stream()
						.map(policy -> policy.user.getEntity())
						.collect(toSet()),
				Map.of(path, policies.stream()
						.map(policyUser -> createEntityGroupInformation(path, policyUser))
						.collect(toList())));

		server.stubFor(WireMock.post("/unity/group-members-multi/%2F")
				.withRequestBody(new ContainsPattern(path))
				.willReturn(aResponse().withStatus(200)
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBody(new ObjectMapper().writeValueAsString(multiGroupMembers))));
	}

	private MultiGroupMembers.EntityGroupAttributes createEntityGroupInformation(String path, PolicyUser policyAcceptance) {
		try {
			final List<AttributeExt> attributes = new ArrayList<>(policyAcceptance.user.getAttributes().values().stream()
					.flatMap(Collection::stream)
					.map(attribute -> new AttributeExt(attribute, true))
					.collect(toList()));
			attributes.add(new AttributeExt(new Attribute(
					FURMS_POLICY_ACCEPTANCE_STATE_ATTRIBUTE, STRING,
					path,
					List.of(objectMapper.writeValueAsString(new PolicyAcceptanceUnityMock(
							policyAcceptance.policyId,
							1,
							ACCEPTED.name(),
							LocalDateTime.now().toInstant(ZoneOffset.UTC))))),
					true));
			return new MultiGroupMembers.EntityGroupAttributes(policyAcceptance.user.getEntity().getEntityInformation().getId(), attributes);
		} catch (JsonProcessingException e) {
			return null;
		}
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

	private static class PolicyUser {
		public final String policyId;
		public final TestUser user;

		public PolicyUser(String policyId, TestUser user) {
			this.policyId = policyId;
			this.user = user;
		}
	}

	private static class PolicyAcceptanceUnityMock {
		public final String policyDocumentId;
		public final int policyDocumentRevision;
		public final String acceptanceStatus;
		public final Instant decisionTs;

		public PolicyAcceptanceUnityMock(String policyDocumentId, int policyDocumentRevision, String acceptanceStatus, Instant decisionTs) {
			this.policyDocumentId = policyDocumentId;
			this.policyDocumentRevision = policyDocumentRevision;
			this.acceptanceStatus = acceptanceStatus;
			this.decisionTs = decisionTs;
		}
	}

}
