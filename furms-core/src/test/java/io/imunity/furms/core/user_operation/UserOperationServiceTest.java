/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.core.user_operation;

import io.imunity.furms.api.authz.AuthzService;
import io.imunity.furms.api.sites.SiteService;
import io.imunity.furms.api.ssh_keys.SSHKeyService;
import io.imunity.furms.api.validation.exceptions.UserInstallationOnSiteIsNotTerminalException;
import io.imunity.furms.domain.policy_documents.PolicyAcceptanceAtSite;
import io.imunity.furms.domain.policy_documents.PolicyId;
import io.imunity.furms.domain.policy_documents.UserPolicyAcceptancesWithServicePolicies;
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
import io.imunity.furms.spi.resource_access.ResourceAccessRepository;
import io.imunity.furms.spi.sites.SiteRepository;
import io.imunity.furms.spi.user_operation.UserOperationRepository;
import io.imunity.furms.spi.user_site_access.UserSiteAccessRepository;
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
import org.springframework.context.ApplicationEventPublisher;
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
	private SiteRepository siteRepository;
	@Autowired
	private UsersDAO usersDAO;
	@Autowired
	private AuthzService authzService;
	@Autowired
	private SiteService siteService;
	@Autowired
	private PolicyDocumentServiceHelper policyService;
	@Autowired
	private SSHKeyService sshKeyService;
	@Autowired
	private ResourceAccessRepository resourceAccessRepository;
	@Autowired
	private UserSiteAccessRepository userSiteAccessRepository;
	@Autowired
	private ApplicationEventPublisher publisher;

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

		when(usersDAO.getFenixUserId(userId)).thenReturn(fenixUserId);
		when(siteService.findUserSites(userId)).thenReturn(Set.of(
				Site.builder().id("id1").name("name1").oauthClientId("oauth1").connectionInfo("conn1").build(),
				Site.builder().id("id2").name("name2").oauthClientId("oauth2").connectionInfo("conn2").build(),
				Site.builder().id("id3").name("name3").oauthClientId("oauth3").connectionInfo("conn3").build()));
		when(repository.findAllUserAdditionsWithSiteAndProjectBySiteId(fenixUserId.id, "id1")).thenReturn(Set.of(
				UserAdditionWithProject.builder().projectId("project11").userId("remoteUser11").build()));
		when(repository.findAllUserAdditionsWithSiteAndProjectBySiteId(fenixUserId.id, "id2")).thenReturn(Set.of(
				UserAdditionWithProject.builder().projectId("project21").userId("remoteUser21").build(),
				UserAdditionWithProject.builder().projectId("project22").userId("remoteUser22").build()));
		when(repository.findAllUserAdditionsWithSiteAndProjectBySiteId(fenixUserId.id, "id3")).thenReturn(Set.of(
				UserAdditionWithProject.builder().projectId("project31").userId("remoteUser31").build(),
				UserAdditionWithProject.builder().projectId("project32").userId("remoteUser32").build(),
				UserAdditionWithProject.builder().projectId("project33").userId("remoteUser33").build()));
		when(policyService.findSitePolicyAcceptancesByUserId(fenixUserId)).thenReturn(Set.of(
				PolicyAcceptanceAtSite.builder().siteId("id1").policyDocumentId(policy1).policyDocumentRevision(1).build(),
				PolicyAcceptanceAtSite.builder().siteId("id1").policyDocumentId(policy1).policyDocumentRevision(2).build(),
				PolicyAcceptanceAtSite.builder().siteId("id2").policyDocumentId(policy2).build(),
				PolicyAcceptanceAtSite.builder().siteId("id3").policyDocumentId(policy3).build()));
		when(policyService.findServicesPolicyAcceptancesByUserId(fenixUserId)).thenReturn(Set.of(
				PolicyAcceptanceAtSite.builder().siteId("id1").policyDocumentId(policy1).build(),
				PolicyAcceptanceAtSite.builder().siteId("id1").policyDocumentId(policy2).build(),
				PolicyAcceptanceAtSite.builder().siteId("id2").policyDocumentId(policy1).build(),
				PolicyAcceptanceAtSite.builder().siteId("id2").policyDocumentId(policy2).build(),
				PolicyAcceptanceAtSite.builder().siteId("id2").policyDocumentId(policy3).build(),
				PolicyAcceptanceAtSite.builder().siteId("id3").policyDocumentId(policy1).build()));

		//when
		final Set<SiteUser> userSitesInstallations = service.findUserSitesInstallations(userId);

		//then
		assertThat(userSitesInstallations).hasSize(3);
		userSitesInstallations.stream()
				.filter(site -> site.siteId.equals("id1"))
				.forEach(site ->{
					assertThat(site.projectMemberships).hasSize(1);
					assertThat(site.siteOauthClientId).isEqualTo("oauth1");
					assertThat(site.sitePolicyAcceptance.policyDocumentRevision).isEqualTo(2);
					assertThat(site.servicesPolicyAcceptance).hasSize(2);
				});
		userSitesInstallations.stream()
				.filter(site -> site.siteId.equals("id2"))
				.forEach(site ->{
					assertThat(site.projectMemberships).hasSize(2);
					assertThat(site.siteOauthClientId).isEqualTo("oauth2");
					assertThat(site.sitePolicyAcceptance).isNotNull();
					assertThat(site.servicesPolicyAcceptance).hasSize(3);
				});
		userSitesInstallations.stream()
				.filter(site -> site.siteId.equals("id3"))
				.forEach(site ->{
					assertThat(site.projectMemberships).hasSize(3);
					assertThat(site.siteOauthClientId).isEqualTo("oauth3");
					assertThat(site.sitePolicyAcceptance).isNotNull();
					assertThat(site.servicesPolicyAcceptance).hasSize(1);
				});
	}

	@Test
	void shouldCreateUserAddition() {
		//given
		SiteId siteId = new SiteId("siteId", new SiteExternalId("id"));
		String projectId = "projectId";
		PersistentId userId = new PersistentId("userId");
		FenixUserId fenixUserId = new FenixUserId("id");
		FURMSUser user = FURMSUser.builder()
			.id(userId)
			.fenixUserId(fenixUserId)
			.email("email")
			.build();
		UserPolicyAcceptancesWithServicePolicies userPolicyAcceptancesWithServicePolicies = new UserPolicyAcceptancesWithServicePolicies(user, Set.of(), Optional.empty(), Set.of());

		service.createUserAdditions(siteId, projectId, new UserPolicyAcceptancesWithServicePolicies(user, Set.of(), Optional.empty(), Set.of()));

		//then
		orderVerifier.verify(repository).create(any(UserAddition.class));
		orderVerifier.verify(siteAgentUserService).addUser(any(UserAddition.class), eq(userPolicyAcceptancesWithServicePolicies));
	}

	@Test
	void shouldNotCreateUserAddition() {
		SiteId siteId = new SiteId("siteId", new SiteExternalId("id"));
		String projectId = "projectId";
		PersistentId userId = new PersistentId("userId");
		FenixUserId id = new FenixUserId("id");
		FURMSUser user = FURMSUser.builder()
			.id(userId)
			.fenixUserId(id)
			.email("email")
			.build();
		//when
		when(repository.existsByUserIdAndSiteIdAndProjectId(id, siteId.id, projectId)).thenReturn(true);

		//then
		assertThrows(IllegalArgumentException.class, () -> service.createUserAdditions(siteId, projectId, new UserPolicyAcceptancesWithServicePolicies(user, Set.of(), Optional.empty(), Set.of())));
	}

	@ParameterizedTest
	@EnumSource(value = UserStatus.class, names = {"REMOVAL_FAILED", "ADDED"})
	void shouldCreateUserRemoval(UserStatus status) {
		String projectId = "projectId";
		PersistentId userId = new PersistentId("userId");
		UserAddition userAddition = UserAddition.builder()
			.status(status)
			.build();

		when(usersDAO.findById(userId)).thenReturn(Optional.of(FURMSUser.builder()
			.email("email")
			.fenixUserId(new FenixUserId("userId"))
			.build()));
		when(repository.findAllUserAdditions(projectId, userId.id)).thenReturn(Set.of(userAddition));
		service.createUserRemovals(projectId, userId);

		//then
		orderVerifier.verify(repository).update(any(UserAddition.class));
		orderVerifier.verify(siteAgentUserService).removeUser(any(UserAddition.class));
	}

	@ParameterizedTest
	@EnumSource(value = UserStatus.class, names = {"REMOVAL_FAILED", "ADDED"}, mode = EXCLUDE)
	void shouldNotCreateUserRemoval(UserStatus status) {
		String projectId = "projectId";
		PersistentId userId = new PersistentId("userId");
		UserAddition userAddition = UserAddition.builder()
			.status(status)
			.build();

		when(usersDAO.findById(userId)).thenReturn(Optional.of(FURMSUser.builder()
			.email("email")
			.fenixUserId(new FenixUserId("userId"))
			.build()));
		when(repository.findAllUserAdditions(projectId, userId.id)).thenReturn(Set.of(userAddition));

		//then
		assertThrows(UserInstallationOnSiteIsNotTerminalException.class, () -> service.createUserRemovals(projectId, userId));
	}

	@ParameterizedTest
	@EnumSource(value = UserStatus.class, names = {"REMOVAL_FAILED", "ADDED"}, mode = EXCLUDE)
	void shouldRemoveResourceAccess(UserStatus status) {
		String projectId = "projectId";
		PersistentId userId = new PersistentId("userId");
		FenixUserId fenixUserId = new FenixUserId("userId");

		when(usersDAO.findById(userId)).thenReturn(Optional.of(FURMSUser.builder()
			.email("email")
			.fenixUserId(fenixUserId)
			.build()));
		when(repository.findAllUserAdditions(projectId, userId.id)).thenReturn(Set.of());
		service.createUserRemovals(projectId, userId);

		//then
		verify(resourceAccessRepository).deleteByUserAndProjectId(fenixUserId, "projectId");
	}
}