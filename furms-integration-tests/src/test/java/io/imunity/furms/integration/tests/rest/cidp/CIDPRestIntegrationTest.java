/*
 * Copyright (c) 2021 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.integration.tests.rest.cidp;

import static io.imunity.furms.domain.project_installation.ProjectInstallationStatus.INSTALLED;
import static io.imunity.furms.domain.resource_access.AccessStatus.GRANT_PENDING;
import static io.imunity.furms.integration.tests.tools.DefaultDataBuilders.defaultCommunity;
import static io.imunity.furms.integration.tests.tools.DefaultDataBuilders.defaultCommunityAllocation;
import static io.imunity.furms.integration.tests.tools.DefaultDataBuilders.defaultGenericGroup;
import static io.imunity.furms.integration.tests.tools.DefaultDataBuilders.defaultPolicy;
import static io.imunity.furms.integration.tests.tools.DefaultDataBuilders.defaultProject;
import static io.imunity.furms.integration.tests.tools.DefaultDataBuilders.defaultProjectAllocation;
import static io.imunity.furms.integration.tests.tools.DefaultDataBuilders.defaultProjectInstallationJob;
import static io.imunity.furms.integration.tests.tools.DefaultDataBuilders.defaultResourceCredit;
import static io.imunity.furms.integration.tests.tools.DefaultDataBuilders.defaultResourceType;
import static io.imunity.furms.integration.tests.tools.DefaultDataBuilders.defaultService;
import static io.imunity.furms.integration.tests.tools.DefaultDataBuilders.defaultSite;
import static io.imunity.furms.integration.tests.tools.DefaultDataBuilders.defaultUserAddition;
import static io.imunity.furms.integration.tests.tools.users.TestUsersProvider.basicUser;
import static io.imunity.furms.rest.admin.AcceptanceStatus.ACCEPTED;
import static io.imunity.furms.unity.common.UnityConst.FURMS_POLICY_ACCEPTANCE_STATE_ATTRIBUTE;
import static io.imunity.furms.unity.common.UnityConst.STRING;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;

import io.imunity.furms.domain.generic_groups.GenericGroupId;
import io.imunity.furms.domain.generic_groups.GenericGroupMembership;
import io.imunity.furms.domain.policy_documents.PolicyId;
import io.imunity.furms.domain.project_installation.ProjectInstallationStatus;
import io.imunity.furms.domain.resource_access.GrantAccess;
import io.imunity.furms.domain.site_agent.CorrelationId;
import io.imunity.furms.domain.sites.Site;
import io.imunity.furms.domain.sites.SiteId;
import io.imunity.furms.domain.ssh_keys.InstalledSSHKey;
import io.imunity.furms.domain.ssh_keys.SSHKey;
import io.imunity.furms.domain.user_operation.UserAddition;
import io.imunity.furms.domain.user_operation.UserStatus;
import io.imunity.furms.domain.users.FenixUserId;
import io.imunity.furms.domain.users.PersistentId;
import io.imunity.furms.integration.tests.IntegrationTestBase;
import io.imunity.furms.integration.tests.tools.PolicyAcceptanceMockUtils;
import io.imunity.furms.integration.tests.tools.users.TestUser;
import pl.edu.icm.unity.types.basic.Attribute;

public class CIDPRestIntegrationTest extends IntegrationTestBase {

	@Value("${furms.psk.centralIdPUser}")
	private String centralIdPUser;

	@Value("${furms.psk.centralIdPSecret}")
	private String centralIdPSecret;

	private PolicyAcceptanceMockUtils policyAcceptanceMockUtils;

	@BeforeEach
	void setUp() {
		policyAcceptanceMockUtils = new PolicyAcceptanceMockUtils(objectMapper, server);
	}

	@Test
	void shouldFindUserRecordByFenixUserId() throws Exception {
		//given
		Site.SiteBuilder siteBuilder = defaultSite();
		final String siteId = siteRepository.create(siteBuilder.build(), siteBuilder.build().getExternalId());
		final PolicyId policyId = createPolicy(siteId);
		final Site site = siteBuilder
				.id(siteId)
				.oauthClientId("siteOauth")
				.policyId(policyId)
				.name("site1")
				.build();
		//just to save oauthClientId and policy
		siteRepository.update(site);

		final TestUser testUser = basicUser();

		final String communityId = communityRepository.create(defaultCommunity()
				.name(UUID.randomUUID().toString())
				.build());
		final String projectId = createProject(communityId);
		final String serviceId = infraServiceRepository.create(defaultService()
				.siteId(site.getId())
				.policyId(site.getPolicyId())
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

		final String path = "/fenix/communities/"+communityId+"/projects/"+projectId+"/users";
		policyAcceptanceMockUtils.createPolicyAcceptancesMock(path, List.of(
				new PolicyAcceptanceMockUtils.PolicyUser(site.getPolicyId().id.toString(), testUser)));

		testUser.getAttributes().put(path, Set.of(new Attribute(
						FURMS_POLICY_ACCEPTANCE_STATE_ATTRIBUTE, STRING,
						path,
						List.of(objectMapper.writeValueAsString(new PolicyAcceptanceMockUtils.PolicyAcceptanceUnityMock(
								site.getPolicyId().id.toString(),
								1,
								ACCEPTED.name(),
								LocalDateTime.now().toInstant(ZoneOffset.UTC)))))));
		setupUser(testUser);

		createSiteInstalledProject(projectId, site.getId(), INSTALLED);
		createUserSite(projectId, site.getId(), testUser);
		createSSHKey(site.getId(), "ssh-key", testUser);
		final String groupName = "groupName";
		createGroup(communityId, groupName, testUser);

		//when
		mockMvc.perform(get("/rest-api/v1/cidp/user/{fenixUserId}", testUser.getFenixId())
				.with(httpBasic(centralIdPUser, centralIdPSecret)))
				.andDo(print())
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.user.fenixIdentifier").value(testUser.getFenixId()))
				.andExpect(jsonPath("$.user.title").isEmpty())
				.andExpect(jsonPath("$.user.firstname").value("Ahsoka"))
				.andExpect(jsonPath("$.user.lastname").value("Thano"))
				.andExpect(jsonPath("$.user.email", containsString("jedi_office@domain.com")))
				.andExpect(jsonPath("$.user.affiliation.name").value("Ahsoka"))
				.andExpect(jsonPath("$.user.affiliation.email", containsString("jedi_office@domain.com")))
				.andExpect(jsonPath("$.userStatus").value("ENABLED"))
				.andExpect(jsonPath("$.siteAccess.[0].siteId").value(site.getId()))
				.andExpect(jsonPath("$.siteAccess.[0].siteOauthClientId").value(site.getOauthClientId()))
				.andExpect(jsonPath("$.siteAccess.[0].projectMemberships.[0].localUserId").value(testUser.getFenixId()))
				.andExpect(jsonPath("$.siteAccess.[0].projectMemberships.[0].projectId").value(projectId))
				.andExpect(jsonPath("$.siteAccess.[0].sitePolicyAcceptance.policyId").value(site.getPolicyId().id.toString()))
				.andExpect(jsonPath("$.siteAccess.[0].sitePolicyAcceptance.acceptanceStatus").value(ACCEPTED.name()))
				.andExpect(jsonPath("$.siteAccess.[0].sitePolicyAcceptance.processedOn").isNotEmpty())
				.andExpect(jsonPath("$.siteAccess.[0].sitePolicyAcceptance.revision").value(1))
				.andExpect(jsonPath("$.siteAccess.[0].servicesPolicyAcceptance.[0].policyId").value(site.getPolicyId().id.toString()))
				.andExpect(jsonPath("$.siteAccess.[0].servicesPolicyAcceptance.[0].acceptanceStatus").value(ACCEPTED.name()))
				.andExpect(jsonPath("$.siteAccess.[0].servicesPolicyAcceptance.[0].processedOn").isNotEmpty())
				.andExpect(jsonPath("$.siteAccess.[0].servicesPolicyAcceptance.[0].revision").value(1))
				.andExpect(jsonPath("$.siteAccess.[0].sshKeys.[0].siteId", equalTo(site.getId())))
				.andExpect(jsonPath("$.siteAccess.[0].sshKeys.[0].sshKeys", equalTo(List.of("ssh-key"))))
				.andExpect(jsonPath("$.groupAccess.[0].communityId").value(communityId))
				.andExpect(jsonPath("$.groupAccess.[0].groups", equalTo(List.of(groupName))));

	}

	private String createSiteInstalledProject(String projectId, String siteId, ProjectInstallationStatus status) {
		return projectOperationRepository.create(defaultProjectInstallationJob()
				.projectId(projectId)
				.siteId(siteId)
				.status(status)
				.build());
	}

	private void createUserSite(String projectId, String siteId, TestUser testUser) {
		final String correlationId = UUID.randomUUID().toString();
		userOperationRepository.create(defaultUserAddition()
				.projectId(projectId)
				.siteId(new SiteId(siteId))
				.userId(testUser.getFenixId())
				.correlationId(new CorrelationId(correlationId))
				.build());
		final UserAddition userAddition = userOperationRepository.findAdditionByCorrelationId(new CorrelationId(correlationId));
		userOperationRepository.update(UserAddition.builder()
				.id(userAddition.id)
				.userId(userAddition.userId)
				.correlationId(userAddition.correlationId)
				.siteId(userAddition.siteId)
				.projectId(userAddition.projectId)
				.uid(testUser.getFenixId())
				.status(UserStatus.ADDED)
				.build());
	}

	private String createProject(String communityId) {
		return projectRepository.create(defaultProject()
				.communityId(communityId)
				.name(UUID.randomUUID().toString())
				.leaderId(new PersistentId(ADMIN_USER.getUserId()))
				.build());
	}

	private String createSSHKey(String siteId, String value, TestUser user) {
		final String sshKeyId = sshKeyRepository.create(SSHKey.builder()
				.sites(Set.of(siteId))
				.name(UUID.randomUUID().toString())
				.ownerId(new PersistentId(user.getUserId()))
				.value(value)
				.createTime(LocalDateTime.now())
				.build());
		installedSSHKeyRepository.create(InstalledSSHKey.builder()
				.siteId(siteId)
				.sshkeyId(sshKeyId)
				.value(value)
				.build());
		return sshKeyId;
	}

	private PolicyId createPolicy(String siteId) {
		return policyDocumentRepository.create(defaultPolicy()
				.siteId(siteId)
				.name(UUID.randomUUID().toString())
				.build());
	}

	private GenericGroupId createGroup(String communityId, String groupName, TestUser testUser) {
		final GenericGroupId genericGroupId = genericGroupRepository.create(defaultGenericGroup()
				.communityId(communityId)
				.name(groupName)
				.build());
		genericGroupRepository.createMembership(GenericGroupMembership.builder()
				.genericGroupId(genericGroupId.id)
				.fenixUserId(testUser.getFenixId())
				.utcMemberSince(LocalDateTime.now().minusDays(1))
				.build());
		return genericGroupId;
	}

}
