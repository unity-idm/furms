/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.core.resource_access;

import io.imunity.furms.api.resource_access.ResourceAccessService;
import io.imunity.furms.core.MockedTransactionManager;
import io.imunity.furms.core.policy_documents.PolicyNotificationService;
import io.imunity.furms.domain.resource_access.AccessStatus;
import io.imunity.furms.domain.resource_access.GrantAccess;
import io.imunity.furms.domain.sites.SiteId;
import io.imunity.furms.domain.user_operation.UserStatus;
import io.imunity.furms.domain.users.FenixUserId;
import io.imunity.furms.site.api.site_agent.SiteAgentResourceAccessService;
import io.imunity.furms.spi.resource_access.ResourceAccessRepository;
import io.imunity.furms.spi.user_operation.UserOperationRepository;
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
import java.util.UUID;

import static io.imunity.furms.domain.resource_access.AccessStatus.GRANT_FAILED;
import static io.imunity.furms.domain.resource_access.AccessStatus.GRANT_PENDING;
import static io.imunity.furms.domain.resource_access.AccessStatus.USER_INSTALLING;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.params.provider.EnumSource.Mode.EXCLUDE;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest(classes = {ResourceAccessLauncher.class, Config.class})
@ExtendWith(MockitoExtension.class)
class ResourceAccessServiceTest {

	@Autowired
	private SiteAgentResourceAccessService siteAgentResourceAccessService;
	@Autowired
	private ResourceAccessRepository repository;
	@Autowired
	private UserOperationRepository userRepository;
	@Autowired
	private PolicyNotificationService policyNotificationService;
	@Autowired
	private ApplicationEventPublisher publisher;
	@Autowired
	private MockedTransactionManager mockedTransactionManager;

	@Autowired
	private ResourceAccessService service;
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
		orderVerifier = inOrder(repository, siteAgentResourceAccessService, policyNotificationService);
	}

	@Test
	void shouldGrantAccessForInstalledUser() {
		UUID grantId = UUID.randomUUID();
		FenixUserId fenixUserId = new FenixUserId("userId");
		GrantAccess grantAccess = GrantAccess.builder()
			.siteId(new SiteId("siteId", "externalId"))
			.projectId("projectId")
			.fenixUserId(fenixUserId)
			.build();
		//when
		when(repository.create(any(), eq(grantAccess), eq(GRANT_PENDING))).thenReturn(grantId);
		when(repository.exists(grantAccess)).thenReturn(false);
		when(userRepository.findAdditionStatus("siteId", "projectId", fenixUserId)).thenReturn(Optional.of(UserStatus.ADDED));

		service.grantAccess(grantAccess);

		//then
		orderVerifier.verify(repository).create(any(), eq(grantAccess), eq(GRANT_PENDING));
		orderVerifier.verify(policyNotificationService).notifyAboutAllNotAcceptedPolicies("siteId", fenixUserId, grantId.toString());
		orderVerifier.verify(siteAgentResourceAccessService).grantAccess(any(), eq(grantAccess));
	}

	@Test
	void shouldGrantAccessForNonInstalledUser() {
		UUID grantId = UUID.randomUUID();
		FenixUserId fenixUserId = new FenixUserId("userId");
		SiteId siteId = new SiteId("siteId", "externalId");
		GrantAccess grantAccess = GrantAccess.builder()
			.siteId(siteId)
			.projectId("projectId")
			.fenixUserId(fenixUserId)
			.build();
		//when
		when(repository.exists(grantAccess)).thenReturn(false);
		when(repository.create(any(), eq(grantAccess), eq(USER_INSTALLING))).thenReturn(grantId);
		when(userRepository.findAdditionStatus("siteId", "projectId", fenixUserId)).thenReturn(Optional.empty());

		service.grantAccess(grantAccess);

		//then
		orderVerifier.verify(repository).create(any(), eq(grantAccess), eq(USER_INSTALLING));
	}

	@Test
	void shouldGrantAccessForNonInstalledUserWithAcceptedPolicy() {
		UUID grantId = UUID.randomUUID();
		FenixUserId fenixUserId = new FenixUserId("userId");
		SiteId siteId = new SiteId("siteId", "externalId");
		GrantAccess grantAccess = GrantAccess.builder()
			.siteId(siteId)
			.projectId("projectId")
			.fenixUserId(fenixUserId)
			.build();

		//when
		when(repository.exists(grantAccess)).thenReturn(false);
		when(repository.create(any(), eq(grantAccess), eq(USER_INSTALLING))).thenReturn(grantId);
		when(userRepository.findAdditionStatus("siteId", "projectId", fenixUserId)).thenReturn(Optional.empty());

		service.grantAccess(grantAccess);

		//then
		orderVerifier.verify(repository).create(any(), eq(grantAccess), eq(USER_INSTALLING));
		orderVerifier.verify(policyNotificationService).notifyAboutAllNotAcceptedPolicies("siteId", fenixUserId, grantId.toString());
	}

	@Test
	void shouldGrantAccessForNonInstalledUserWithSiteWithoutPolicy() {
		UUID grantId = UUID.randomUUID();
		FenixUserId fenixUserId = new FenixUserId("userId");
		SiteId siteId = new SiteId("siteId", "externalId");
		GrantAccess grantAccess = GrantAccess.builder()
			.siteId(siteId)
			.projectId("projectId")
			.fenixUserId(fenixUserId)
			.build();

		//when
		when(repository.exists(grantAccess)).thenReturn(false);
		when(repository.create(any(), eq(grantAccess), eq(USER_INSTALLING))).thenReturn(grantId);
		when(userRepository.findAdditionStatus("siteId", "projectId", fenixUserId)).thenReturn(Optional.empty());

		service.grantAccess(grantAccess);

		//then
		orderVerifier.verify(repository).create(any(), eq(grantAccess), eq(USER_INSTALLING));
		orderVerifier.verify(policyNotificationService).notifyAboutAllNotAcceptedPolicies("siteId", fenixUserId, grantId.toString());
	}

	@Test
	void shouldGrantAccessForInstallingUser() {
		UUID grantId = UUID.randomUUID();
		FenixUserId fenixUserId = new FenixUserId("userId");
		SiteId siteId = new SiteId("siteId", "externalId");
		GrantAccess grantAccess = GrantAccess.builder()
			.siteId(siteId)
			.projectId("projectId")
			.fenixUserId(fenixUserId)
			.build();
		//when
		when(repository.exists(grantAccess)).thenReturn(false);
		when(repository.create(any(), eq(grantAccess), eq(USER_INSTALLING))).thenReturn(grantId);
		when(userRepository.findAdditionStatus("siteId", "projectId", fenixUserId)).thenReturn(Optional.of(UserStatus.ADDING_PENDING));
		when(userRepository.findAdditionStatus("siteId", "projectId", fenixUserId)).thenReturn(Optional.of(UserStatus.ADDING_PENDING));

		service.grantAccess(grantAccess);

		//then
		orderVerifier.verify(repository).create(any(), eq(grantAccess), eq(USER_INSTALLING));
		orderVerifier.verify(policyNotificationService).notifyAboutAllNotAcceptedPolicies("siteId", fenixUserId, grantId.toString());
	}

	@Test
	void shouldNotGrantAccessIfExists() {
		GrantAccess grantAccess = GrantAccess.builder()
			.build();
		//when
		when(repository.exists(grantAccess)).thenReturn(true);

		//then
		assertThrows(IllegalArgumentException.class, () -> service.grantAccess(grantAccess));
	}

	@ParameterizedTest
	@EnumSource(value = AccessStatus.class, names = {"REVOKE_FAILED", "GRANTED"})
	void shouldRevokeAccess(AccessStatus status) {
		FenixUserId userId = new FenixUserId("id");

		GrantAccess grantAccess = GrantAccess.builder()
			.fenixUserId(userId)
			.allocationId("id")
			.build();

		//when
		when(repository.findCurrentStatus(userId, "id")).thenReturn(status);
		service.revokeAccess(grantAccess);

		//then
		orderVerifier.verify(repository).update(any(), eq(grantAccess), eq(AccessStatus.REVOKE_PENDING));
		orderVerifier.verify(siteAgentResourceAccessService).revokeAccess(any(), eq(grantAccess));
	}

	@ParameterizedTest
	@EnumSource(value = AccessStatus.class, names = {"REVOKE_FAILED", "GRANTED", "GRANT_FAILED"}, mode = EXCLUDE)
	void shouldNotRevokeAccessIfStateIsNotTransitionalTo(AccessStatus status) {
		FenixUserId userId = new FenixUserId("id");

		GrantAccess grantAccess = GrantAccess.builder()
			.fenixUserId(userId)
			.allocationId("id")
			.build();

		//when
		when(repository.findCurrentStatus(userId, "id")).thenReturn(status);

		//then
		assertThrows(IllegalArgumentException.class, () -> service.revokeAccess(grantAccess));
	}

	@Test
	void shouldOnlyDeleteAccessGrantIfFails() {
		FenixUserId userId = new FenixUserId("id");
		GrantAccess grantAccess = GrantAccess.builder()
			.fenixUserId(userId)
			.allocationId("id")
			.build();
		//when
		when(repository.findCurrentStatus(userId, "id")).thenReturn(GRANT_FAILED);
		service.revokeAccess(grantAccess);

		//then
		verify(repository).deleteByUserAndAllocationId(userId, "id");
		verify(repository, times(0)).update(any(), eq(grantAccess), eq(AccessStatus.REVOKE_PENDING));
		verify(siteAgentResourceAccessService, times(0)).revokeAccess(any(), eq(grantAccess));	}
}