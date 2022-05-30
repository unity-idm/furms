/*
 * Copyright (c) 2021 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.integration.tests.rest.site;

import io.imunity.furms.domain.communities.CommunityId;
import io.imunity.furms.domain.project_installation.ProjectInstallationStatus;
import io.imunity.furms.domain.projects.ProjectId;
import io.imunity.furms.domain.site_agent.CorrelationId;
import io.imunity.furms.domain.sites.Site;
import io.imunity.furms.domain.sites.SiteExternalId;
import io.imunity.furms.domain.sites.SiteId;
import io.imunity.furms.domain.ssh_keys.InstalledSSHKey;
import io.imunity.furms.domain.ssh_keys.SSHKey;
import io.imunity.furms.domain.ssh_keys.SSHKeyId;
import io.imunity.furms.domain.user_operation.UserAddition;
import io.imunity.furms.domain.user_operation.UserStatus;
import io.imunity.furms.domain.users.PersistentId;
import io.imunity.furms.integration.tests.IntegrationTestBase;
import io.imunity.furms.integration.tests.tools.users.TestUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static io.imunity.furms.domain.project_installation.ProjectInstallationStatus.INSTALLED;
import static io.imunity.furms.domain.project_installation.ProjectInstallationStatus.PENDING;
import static io.imunity.furms.integration.tests.tools.DefaultDataBuilders.defaultCommunity;
import static io.imunity.furms.integration.tests.tools.DefaultDataBuilders.defaultProject;
import static io.imunity.furms.integration.tests.tools.DefaultDataBuilders.defaultProjectInstallationJob;
import static io.imunity.furms.integration.tests.tools.DefaultDataBuilders.defaultSite;
import static io.imunity.furms.integration.tests.tools.DefaultDataBuilders.defaultUserAddition;
import static io.imunity.furms.integration.tests.tools.users.TestUsersProvider.basicUser;
import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.in;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class SiteInstalledProjectsIntegrationTest extends IntegrationTestBase {

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
	void shouldFindAllInstalledProjectsForSpecificSite() throws Exception {
		//given
		final CommunityId communityId = communityRepository.create(defaultCommunity()
				.name(UUID.randomUUID().toString())
				.build());
		final ProjectId projectId1 = createProject(communityId);
		final ProjectId projectId2 = createProject(communityId);
		final ProjectId projectId3 = createProject(communityId);
		createSiteInstalledProject(projectId1, site.getId(), INSTALLED);
		createSiteInstalledProject(projectId2, site.getId(), INSTALLED);
		createSiteInstalledProject(projectId3, site.getId(), PENDING);
		createSiteInstalledProject(projectId3, darkSite.getId(), INSTALLED);
		final Set<String> expectedProjects = Set.of(projectId1.id.toString(), projectId2.id.toString());

		final TestUser user = basicUser();
		user.addSiteAdmin(site.getId());
		user.addProjectAdmin(communityId, projectId1);
		user.addProjectAdmin(communityId, projectId2);
		user.addProjectAdmin(communityId, projectId3);
		setupUser(user);

		//when
		mockMvc.perform(get("/rest-api/v1/sites/{siteId}/projectInstallations", site.getId().id)
				.with(user.getHttpBasic()))
				.andDo(print())
				.andExpect(status().isOk())
				.andExpect(jsonPath("$", hasSize(2)))
				.andExpect(jsonPath("$.[0].project.id", in(expectedProjects)))
				.andExpect(jsonPath("$.[0].installationStatus", equalTo(INSTALLED.name())))
				.andExpect(jsonPath("$.[1].project.id", in(expectedProjects)))
				.andExpect(jsonPath("$.[1].installationStatus", equalTo(INSTALLED.name())));
	}

	@Test
	void shouldReturnNotFoundIfSiteDoesNotExistDuringGettingAllInstalledProjects() throws Exception {
		//when
		mockMvc.perform(adminGET("/rest-api/v1/sites/{siteId}/projectInstallations", UUID.randomUUID().toString()))
				.andDo(print())
				.andExpect(status().isNotFound());
	}

	@Test
	void shouldReturnForbiddenIfSiteDoesNotBelongToUserDuringGettingAllInstalledProjects() throws Exception {
		final TestUser testUser = basicUser();
		setupUser(testUser);

		//when
		mockMvc.perform(get("/rest-api/v1/sites/{siteId}/projectInstallations", site.getId().id)
				.with(testUser.getHttpBasic()))
				.andDo(print())
				.andExpect(status().isForbidden());
	}

	@Test
	void shouldFindAllUsersThatHaveAccessToSite() throws Exception {
		//given
		final CommunityId communityId = communityRepository.create(defaultCommunity()
				.name(UUID.randomUUID().toString())
				.build());
		final ProjectId projectId1 = createProject(communityId);
		final ProjectId projectId2 = createProject(communityId);
		final ProjectId projectId3 = createProject(communityId);
		final ProjectId projectId4 = createProject(communityId);
		final TestUser otherUser = basicUser();
		otherUser.addSiteAdmin(site.getId());
		setupUser(otherUser);

		createUserSite(projectId1, site.getId(), ADMIN_USER);
		createUserSite(projectId2, site.getId(), ADMIN_USER);
		createUserSite(projectId3, site.getId(), ADMIN_USER);
		createUserSite(projectId1, site.getId(), otherUser);
		createUserSite(projectId2, site.getId(), otherUser);
		createUserSite(projectId4, darkSite.getId(), ADMIN_USER);
		createUserSite(projectId4, darkSite.getId(), otherUser);

		final String sshKey1Value = "ssh-rsa " + UUID.randomUUID();
		final String sshKey2Value = "ssh-rsa " + UUID.randomUUID();
		createSSHKey(site.getId(), sshKey1Value, ADMIN_USER);
		createSSHKey(site.getId(), sshKey2Value, otherUser);
		final List<List<String>> sshKeyExpectedValues = List.of(List.of(sshKey1Value), List.of(sshKey2Value));

		//when
		mockMvc.perform(adminGET("/rest-api/v1/sites/{siteId}/users", site.getId().id))
				.andDo(print())
				.andExpect(status().isOk())
				.andExpect(jsonPath("$", hasSize(2)))
				.andExpect(jsonPath("$.[0].user.fenixIdentifier", in(Set.of(ADMIN_USER.getFenixId(), otherUser.getFenixId()))))
				.andExpect(jsonPath("$.[0].uid", in(Set.of(ADMIN_USER.getFenixId(), otherUser.getFenixId()))))
				.andExpect(jsonPath("$.[0].projectIds").value(anyOf(
						containsInAnyOrder(projectId1.id.toString(), projectId2.id.toString(), projectId3.id.toString()),
						containsInAnyOrder(projectId1.id.toString(), projectId2.id.toString()))))
				.andExpect(jsonPath("$.[0].sshKeys", in(sshKeyExpectedValues)))
				.andExpect(jsonPath("$.[1].user.fenixIdentifier", in(Set.of(ADMIN_USER.getFenixId(), otherUser.getFenixId()))))
				.andExpect(jsonPath("$.[1].uid", in(Set.of(ADMIN_USER.getFenixId(), otherUser.getFenixId()))))
				.andExpect(jsonPath("$.[1].projectIds").value(anyOf(
						containsInAnyOrder(projectId1.id.toString(), projectId2.id.toString(), projectId3.id.toString()),
						containsInAnyOrder(projectId1.id.toString(), projectId2.id.toString()))))
				.andExpect(jsonPath("$.[1].sshKeys", in(sshKeyExpectedValues)));
	}

	@Test
	void shouldReturnSSHKeysGroupedByUserIdAndSiteId() throws Exception {
		//given
		final CommunityId communityId = communityRepository.create(defaultCommunity()
				.name(UUID.randomUUID().toString())
				.build());
		final ProjectId projectId1 = createProject(communityId);
		final ProjectId projectId2 = createProject(communityId);
		final TestUser otherUser = basicUser();
		otherUser.addSiteAdmin(site.getId());
		otherUser.addSiteAdmin(darkSite.getId());
		setupUser(otherUser);

		createUserSite(projectId1, site.getId(), otherUser);
		createUserSite(projectId2, site.getId(), otherUser);
		createUserSite(projectId1, darkSite.getId(), otherUser);

		final String sshKeySiteValue = "ssh-rsa " + UUID.randomUUID();
		final String sshKeyDarkSiteValue = "ssh-rsa " + UUID.randomUUID();
		createSSHKey(site.getId(), sshKeySiteValue, otherUser);
		createSSHKey(darkSite.getId(), sshKeyDarkSiteValue, otherUser);

		//when
		mockMvc.perform(get("/rest-api/v1/sites/{siteId}/users", site.getId().id)
				.with(otherUser.getHttpBasic()))
				.andDo(print())
				.andExpect(status().isOk())
				.andExpect(jsonPath("$", hasSize(1)))
				.andExpect(jsonPath("$.[0].sshKeys", equalTo(List.of(sshKeySiteValue))));
		mockMvc.perform(get("/rest-api/v1/sites/{siteId}/users", darkSite.getId().id)
				.with(otherUser.getHttpBasic()))
				.andDo(print())
				.andExpect(status().isOk())
				.andExpect(jsonPath("$", hasSize(1)))
				.andExpect(jsonPath("$.[0].sshKeys", equalTo(List.of(sshKeyDarkSiteValue))));
	}

	private void createSiteInstalledProject(ProjectId projectId, SiteId siteId, ProjectInstallationStatus status) {
		projectOperationRepository.createOrUpdate(defaultProjectInstallationJob()
			.projectId(projectId)
			.siteId(siteId)
			.status(status)
			.build());
	}

	private void createUserSite(ProjectId projectId, SiteId siteId, TestUser testUser) {
		final String correlationId = UUID.randomUUID().toString();
		userOperationRepository.create(defaultUserAddition()
				.projectId(projectId)
				.siteId(siteId)
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

	private ProjectId createProject(CommunityId communityId) {
		return projectRepository.create(defaultProject()
				.communityId(communityId)
				.name(UUID.randomUUID().toString())
				.leaderId(new PersistentId(ADMIN_USER.getUserId()))
				.build());
	}

	private void createSSHKey(SiteId siteId, String value, TestUser user) {
		final SSHKeyId sshKeyId = sshKeyRepository.create(SSHKey.builder()
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
	}
}
