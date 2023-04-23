/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.core.user_operation;

import io.imunity.furms.api.sites.SiteService;
import io.imunity.furms.api.validation.exceptions.UserInstallationOnSiteIsNotTerminalException;
import io.imunity.furms.domain.policy_documents.PolicyAcceptanceAtSite;
import io.imunity.furms.domain.policy_documents.PolicyId;
import io.imunity.furms.domain.policy_documents.UserPolicyAcceptancesWithServicePolicies;
import io.imunity.furms.domain.projects.ProjectId;
import io.imunity.furms.domain.sites.Site;
import io.imunity.furms.domain.sites.SiteExternalId;
import io.imunity.furms.domain.sites.SiteId;
import io.imunity.furms.domain.sites.SiteUser;
import io.imunity.furms.domain.user_operation.UserAddition;
import io.imunity.furms.domain.user_operation.UserAdditionWithProject;
import io.imunity.furms.domain.user_operation.UserStatus;
import io.imunity.furms.domain.users.FURMSUser;
import io.imunity.furms.domain.users.FenixUserId;
import io.imunity.furms.domain.users.PersistentId;
import io.imunity.furms.site.api.site_agent.SiteAgentUserService;
import io.imunity.furms.spi.project_installation.ProjectOperationRepository;
import io.imunity.furms.spi.resource_access.ResourceAccessRepository;
import io.imunity.furms.spi.user_operation.UserOperationRepository;
import io.imunity.furms.spi.users.UsersDAO;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.InOrder;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.params.provider.EnumSource.Mode.EXCLUDE;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest
@ExtendWith(MockitoExtension.class)
class UserOperationServiceTest {
	@Autowired
	private UserOperationRepository repository;
	@Autowired
	private SiteAgentUserService siteAgentUserService;
	@Autowired
	private UsersDAO usersDAO;
	@Autowired
	private SiteService siteService;
	@Autowired
	private PolicyDocumentServiceHelper policyService;
	@Autowired
	private ResourceAccessRepository resourceAccessRepository;
	@Autowired
	private ProjectOperationRepository projectOperationRepository;

	@Autowired
	private UserOperationService service;
	private InOrder orderVerifier;

	@BeforeEach
	void setUp() {
		TransactionSynchronizationManager.initSynchronization();
	}

	@AfterEach
	void clear() {
		TransactionSynchronizationManager.clear();
	}

	@BeforeEach
	void init() {
		orderVerifier = inOrder(repository, siteAgentUserService);
	}

	@Test
	void shouldFindUserSitesInstallations() {
		//given
		final PersistentId userId = new PersistentId("userId");
		final FenixUserId fenixUserId = new FenixUserId("userId@domain.com");
		final PolicyId policy1 = new PolicyId(UUID.randomUUID());
		final PolicyId policy2 = new PolicyId(UUID.randomUUID());
		final PolicyId policy3 = new PolicyId(UUID.randomUUID());

		SiteId siteId = new SiteId(UUID.randomUUID());
		SiteId siteId1 = new SiteId(UUID.randomUUID());
		SiteId siteId2 = new SiteId(UUID.randomUUID());

		when(usersDAO.getFenixUserId(userId)).thenReturn(fenixUserId);
		when(siteService.findUserSites(userId)).thenReturn(Set.of(
				Site.builder().id(siteId).name("name1").oauthClientId("oauth1").connectionInfo("conn1").build(),
				Site.builder().id(siteId1).name("name2").oauthClientId("oauth2").connectionInfo("conn2").build(),
				Site.builder().id(siteId2).name("name3").oauthClientId("oauth3").connectionInfo("conn3").build())
		);
		when(repository.findAllUserAdditionsWithSiteAndProjectBySiteId(fenixUserId, siteId)).thenReturn(Set.of(
				UserAdditionWithProject.builder().projectId(UUID.randomUUID().toString()).userId("remoteUser11").build())
		);
		when(repository.findAllUserAdditionsWithSiteAndProjectBySiteId(fenixUserId, siteId1)).thenReturn(Set.of(
				UserAdditionWithProject.builder().projectId(UUID.randomUUID().toString()).userId("remoteUser21").build(),
				UserAdditionWithProject.builder().projectId(UUID.randomUUID().toString()).userId("remoteUser22").build())
		);
		when(repository.findAllUserAdditionsWithSiteAndProjectBySiteId(fenixUserId, siteId2)).thenReturn(Set.of(
				UserAdditionWithProject.builder().projectId(UUID.randomUUID().toString()).userId("remoteUser31").build(),
				UserAdditionWithProject.builder().projectId(UUID.randomUUID().toString()).userId("remoteUser32").build(),
				UserAdditionWithProject.builder().projectId(UUID.randomUUID().toString()).userId("remoteUser33").build())
		);
		when(policyService.findSitePolicyAcceptancesByUserId(fenixUserId)).thenReturn(Set.of(
				PolicyAcceptanceAtSite.builder().siteId(siteId.id.toString()).policyDocumentId(policy1).policyDocumentRevision(1).build(),
				PolicyAcceptanceAtSite.builder().siteId(siteId.id.toString()).policyDocumentId(policy1).policyDocumentRevision(2).build(),
				PolicyAcceptanceAtSite.builder().siteId(siteId1.id.toString()).policyDocumentId(policy2).build(),
				PolicyAcceptanceAtSite.builder().siteId(siteId2.id.toString()).policyDocumentId(policy3).build())
		);
		when(policyService.findServicesPolicyAcceptancesByUserId(fenixUserId)).thenReturn(Set.of(
				PolicyAcceptanceAtSite.builder().siteId(siteId.id.toString()).policyDocumentId(policy1).build(),
				PolicyAcceptanceAtSite.builder().siteId(siteId.id.toString()).policyDocumentId(policy2).build(),
				PolicyAcceptanceAtSite.builder().siteId(siteId1.id.toString()).policyDocumentId(policy1).build(),
				PolicyAcceptanceAtSite.builder().siteId(siteId1.id.toString()).policyDocumentId(policy2).build(),
				PolicyAcceptanceAtSite.builder().siteId(siteId1.id.toString()).policyDocumentId(policy3).build(),
				PolicyAcceptanceAtSite.builder().siteId(siteId2.id.toString()).policyDocumentId(policy1).build())
		);

		//when
		final Set<SiteUser> userSitesInstallations = service.findUserSitesInstallations(userId);

		//then
		assertThat(userSitesInstallations).hasSize(3);
		userSitesInstallations.stream()
				.filter(site -> site.siteId.equals(siteId))
				.forEach(site ->{
					assertThat(site.projectMemberships).hasSize(1);
					assertThat(site.siteOauthClientId).isEqualTo("oauth1");
					assertThat(site.sitePolicyAcceptance.policyDocumentRevision).isEqualTo(2);
					assertThat(site.servicesPolicyAcceptance).hasSize(2);
				});
		userSitesInstallations.stream()
				.filter(site -> site.siteId.equals(siteId1))
				.forEach(site ->{
					assertThat(site.projectMemberships).hasSize(2);
					assertThat(site.siteOauthClientId).isEqualTo("oauth2");
					assertThat(site.sitePolicyAcceptance).isNotNull();
					assertThat(site.servicesPolicyAcceptance).hasSize(3);
				});
		userSitesInstallations.stream()
				.filter(site -> site.siteId.equals(siteId2))
				.forEach(site ->{
					assertThat(site.projectMemberships).hasSize(3);
					assertThat(site.siteOauthClientId).isEqualTo("oauth3");
					assertThat(site.sitePolicyAcceptance).isNotNull();
					assertThat(site.servicesPolicyAcceptance).hasSize(1);
				});
	}

	@Test
	void shouldCreateUserAdditionAndSandMessageToSite() {
		//given
		SiteId siteId = new SiteId(UUID.randomUUID().toString(), new SiteExternalId("id"));
		ProjectId projectId = new ProjectId(UUID.randomUUID());
		PersistentId userId = new PersistentId("userId");
		FenixUserId fenixUserId = new FenixUserId("id");
		FURMSUser user = FURMSUser.builder()
			.id(userId)
			.fenixUserId(fenixUserId)
			.email("email")
			.build();
		UserPolicyAcceptancesWithServicePolicies userPolicyAcceptancesWithServicePolicies = new UserPolicyAcceptancesWithServicePolicies(user, Set.of(), Optional.empty(), Set.of());
		when(projectOperationRepository.installedProjectExistsBySiteIdAndProjectId(siteId, projectId)).thenReturn(true);

		service.createUserAdditions(siteId, projectId, new UserPolicyAcceptancesWithServicePolicies(user, Set.of(), Optional.empty(), Set.of()));

		//then
		orderVerifier.verify(repository).create(any(UserAddition.class));
		orderVerifier.verify(siteAgentUserService).addUser(any(UserAddition.class), eq(userPolicyAcceptancesWithServicePolicies));
	}

	@Test
	void shouldCreateUserAdditionWithoutSandingMessageToSite() {
		//given
		SiteId siteId = new SiteId(UUID.randomUUID().toString(), new SiteExternalId("id"));
		ProjectId projectId = new ProjectId(UUID.randomUUID());
		PersistentId userId = new PersistentId("userId");
		FenixUserId fenixUserId = new FenixUserId("id");
		FURMSUser user = FURMSUser.builder()
			.id(userId)
			.fenixUserId(fenixUserId)
			.email("email")
			.build();
		UserPolicyAcceptancesWithServicePolicies userPolicyAcceptancesWithServicePolicies = new UserPolicyAcceptancesWithServicePolicies(user, Set.of(), Optional.empty(), Set.of());
		when(projectOperationRepository.installedProjectExistsBySiteIdAndProjectId(siteId, projectId)).thenReturn(false);

		service.createUserAdditions(siteId, projectId, new UserPolicyAcceptancesWithServicePolicies(user, Set.of(), Optional.empty(), Set.of()));

		//then
		orderVerifier.verify(repository).create(any(UserAddition.class));
		verify(siteAgentUserService, times(0)).addUser(any(UserAddition.class),
			eq(userPolicyAcceptancesWithServicePolicies));
	}

	@Test
	void shouldNotCreateUserAddition() {
		SiteId siteId = new SiteId(UUID.randomUUID().toString(), new SiteExternalId("id"));
		ProjectId projectId = new ProjectId(UUID.randomUUID());
		PersistentId userId = new PersistentId("userId");
		FenixUserId id = new FenixUserId("id");
		FURMSUser user = FURMSUser.builder()
			.id(userId)
			.fenixUserId(id)
			.email("email")
			.build();
		//when
		when(repository.existsByUserIdAndSiteIdAndProjectId(id, siteId, projectId)).thenReturn(true);

		//then
		assertThrows(
			IllegalArgumentException.class,
			() -> service.createUserAdditions(siteId, projectId, new UserPolicyAcceptancesWithServicePolicies(user, Set.of(), Optional.empty(), Set.of()))
		);
	}

	@ParameterizedTest
	@EnumSource(value = UserStatus.class, names = {"REMOVAL_FAILED", "ADDED"})
	void shouldCreateUserRemoval(UserStatus status) {
		ProjectId projectId = new ProjectId(UUID.randomUUID());
		FenixUserId userId = new FenixUserId("userId");
		PersistentId id = new PersistentId("userId");
		UserAddition userAddition = UserAddition.builder()
			.projectId(projectId)
			.status(status)
			.build();

		when(usersDAO.findById(id)).thenReturn(Optional.of(FURMSUser.builder()
			.email("email")
			.fenixUserId(userId)
			.build()));
		when(repository.findAllUserAdditions(projectId, userId)).thenReturn(Set.of(userAddition));
		service.createUserRemovals(projectId, id);

		//then
		orderVerifier.verify(repository).update(any(UserAddition.class));
		orderVerifier.verify(siteAgentUserService).removeUser(any(UserAddition.class));
	}

	@ParameterizedTest
	@EnumSource(value = UserStatus.class, names = {"REMOVAL_FAILED", "ADDED"}, mode = EXCLUDE)
	void shouldNotCreateUserRemoval(UserStatus status) {
		ProjectId projectId = new ProjectId(UUID.randomUUID());
		FenixUserId userId = new FenixUserId("userId");
		PersistentId id = new PersistentId("userId");
		UserAddition userAddition = UserAddition.builder()
			.status(status)
			.projectId(projectId)
			.userId(userId)
			.build();

		when(usersDAO.findById(id)).thenReturn(Optional.of(FURMSUser.builder()
			.id(id)
			.email("email")
			.fenixUserId(userId)
			.build()));
		when(repository.findAllUserAdditions(projectId, userId)).thenReturn(Set.of(userAddition));

		//then
		assertThrows(UserInstallationOnSiteIsNotTerminalException.class, () -> service.createUserRemovals(projectId,
			id));
	}

	@ParameterizedTest
	@EnumSource(value = UserStatus.class, names = {"REMOVAL_FAILED", "ADDED"}, mode = EXCLUDE)
	void shouldRemoveResourceAccess(UserStatus status) {
		ProjectId projectId = new ProjectId(UUID.randomUUID());
		PersistentId userId = new PersistentId("userId");
		FenixUserId fenixUserId = new FenixUserId("userId");

		when(usersDAO.findById(userId)).thenReturn(Optional.of(FURMSUser.builder()
			.email("email")
			.fenixUserId(fenixUserId)
			.build()));
		when(repository.findAllUserAdditions(projectId, fenixUserId)).thenReturn(Set.of());
		service.createUserRemovals(projectId, userId);

		//then
		verify(resourceAccessRepository).deleteByUserAndProjectId(fenixUserId, projectId);
	}
}