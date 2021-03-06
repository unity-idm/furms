/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.core.resource_access;

import io.imunity.furms.api.authz.AuthzService;
import io.imunity.furms.api.resource_access.ResourceAccessService;
import io.imunity.furms.core.user_operation.UserOperationService;
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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.Optional;

import static io.imunity.furms.domain.resource_access.AccessStatus.GRANT_FAILED;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.params.provider.EnumSource.Mode.EXCLUDE;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class ResourceAccessServiceTest {
	@Mock
	private SiteAgentResourceAccessService siteAgentResourceAccessService;
	@Mock
	private ResourceAccessRepository repository;
	@Mock
	private UserOperationService userOperationService;
	@Mock
	private UserOperationRepository userRepository;
	@Mock
	private AuthzService authzService;

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
		MockitoAnnotations.initMocks(this);
		service = new ResourceAccessServiceImpl(siteAgentResourceAccessService, repository, userOperationService, userRepository, authzService);
		orderVerifier = inOrder(repository, siteAgentResourceAccessService, userOperationService);
	}

	@Test
	void shouldGrantAccessForInstalledUser() {
		FenixUserId fenixUserId = new FenixUserId("userId");
		GrantAccess grantAccess = GrantAccess.builder()
			.siteId(new SiteId("siteId", "externalId"))
			.projectId("projectId")
			.fenixUserId(fenixUserId)
			.build();
		//when
		when(repository.exists(grantAccess)).thenReturn(false);
		when(userRepository.findAdditionStatus("siteId", "projectId", fenixUserId)).thenReturn(Optional.of(UserStatus.ADDED));

		service.grantAccess(grantAccess);
		for (TransactionSynchronization transactionSynchronization : TransactionSynchronizationManager
			.getSynchronizations()) {
			transactionSynchronization.afterCommit();
		}

		//then
		orderVerifier.verify(repository).create(any(), eq(grantAccess), eq(AccessStatus.GRANT_PENDING));
		orderVerifier.verify(siteAgentResourceAccessService).grantAccess(any(), eq(grantAccess));
	}

	@Test
	void shouldGrantAccessForNonInstalledUser() {
		FenixUserId fenixUserId = new FenixUserId("userId");
		SiteId siteId = new SiteId("siteId", "externalId");
		GrantAccess grantAccess = GrantAccess.builder()
			.siteId(siteId)
			.projectId("projectId")
			.fenixUserId(fenixUserId)
			.build();
		//when
		when(repository.exists(grantAccess)).thenReturn(false);
		when(userRepository.findAdditionStatus("siteId", "projectId", fenixUserId)).thenReturn(Optional.empty());

		service.grantAccess(grantAccess);
		for (TransactionSynchronization transactionSynchronization : TransactionSynchronizationManager
			.getSynchronizations()) {
			transactionSynchronization.afterCommit();
		}

		//then
		orderVerifier.verify(repository).create(any(), eq(grantAccess), eq(AccessStatus.USER_INSTALLING));
		orderVerifier.verify(userOperationService).createUserAdditions(siteId, "projectId", fenixUserId);
	}

	@Test
	void shouldGrantAccessForInstallingUser() {
		FenixUserId fenixUserId = new FenixUserId("userId");
		SiteId siteId = new SiteId("siteId", "externalId");
		GrantAccess grantAccess = GrantAccess.builder()
			.siteId(siteId)
			.projectId("projectId")
			.fenixUserId(fenixUserId)
			.build();
		//when
		when(repository.exists(grantAccess)).thenReturn(false);
		when(userRepository.findAdditionStatus("siteId", "projectId", fenixUserId)).thenReturn(Optional.of(UserStatus.ADDING_PENDING));

		service.grantAccess(grantAccess);
		for (TransactionSynchronization transactionSynchronization : TransactionSynchronizationManager
			.getSynchronizations()) {
			transactionSynchronization.afterCommit();
		}

		//then
		orderVerifier.verify(repository).create(any(), eq(grantAccess), eq(AccessStatus.USER_INSTALLING));
		verify(userOperationService, times(0)).createUserAdditions(siteId, "projectId", fenixUserId);
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
		for (TransactionSynchronization transactionSynchronization : TransactionSynchronizationManager
			.getSynchronizations()) {
			transactionSynchronization.afterCommit();
		}

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