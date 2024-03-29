/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.core.user_operation;

import io.imunity.furms.domain.projects.ProjectId;
import io.imunity.furms.domain.resource_access.AccessStatus;
import io.imunity.furms.domain.resource_access.GrantAccess;
import io.imunity.furms.domain.site_agent.CorrelationId;
import io.imunity.furms.domain.sites.SiteId;
import io.imunity.furms.domain.user_operation.UserAddition;
import io.imunity.furms.domain.user_operation.UserStatus;
import io.imunity.furms.domain.users.FURMSUser;
import io.imunity.furms.domain.users.FenixUserId;
import io.imunity.furms.site.api.site_agent.SiteAgentResourceAccessService;
import io.imunity.furms.site.api.status_updater.UserOperationStatusUpdater;
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

import static io.imunity.furms.domain.user_operation.UserStatus.ADDING_ACKNOWLEDGED;
import static io.imunity.furms.domain.user_operation.UserStatus.ADDING_PENDING;
import static io.imunity.furms.domain.user_operation.UserStatus.REMOVED;
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
class UserOperationStatusUpdaterTest {
	@Autowired
	private SiteAgentResourceAccessService siteAgentResourceAccessService;
	@Autowired
	private UserOperationRepository repository;
	@Autowired
	private ResourceAccessRepository resourceAccessRepository;
	@Autowired
	private UsersDAO usersDAO;
	@Autowired
	private UserOperationStatusUpdater service;

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
		orderVerifier = inOrder(repository, resourceAccessRepository, siteAgentResourceAccessService);
	}

	@ParameterizedTest
	@EnumSource(value = UserStatus.class, names = {"ADDING_PENDING", "ADDING_ACKNOWLEDGED"})
	void shouldUpdateUserAdditionToAdded(UserStatus userStatus) {
		CorrelationId correlationId = CorrelationId.randomID();
		SiteId siteId = new SiteId(UUID.randomUUID().toString(), "externalId");
		ProjectId projectId = new ProjectId(UUID.randomUUID());
		UserAddition userAddition = UserAddition.builder()
			.siteId(siteId)
			.projectId(projectId)
			.userId("userId")
			.correlationId(correlationId)
			.status(UserStatus.ADDED)
			.build();
		when(repository.findAdditionStatusByCorrelationId(correlationId)).thenReturn(Optional.of(userStatus));
		when(repository.findAdditionByCorrelationId(correlationId)).thenReturn(userAddition);

		service.update(userAddition);

		orderVerifier.verify(repository).update(any(UserAddition.class));
		verify(resourceAccessRepository, times(0)).update(any(), any(), eq(AccessStatus.GRANT_PENDING));
	}

	@ParameterizedTest
	@EnumSource(value = UserStatus.class, names = {"ADDING_PENDING", "ADDING_ACKNOWLEDGED"})
	void shouldUpdateUserAdditionToAddedAndStartWaitingGrantAccesses(UserStatus userStatus) {
		CorrelationId correlationId = CorrelationId.randomID();
		FenixUserId fenixUserId = new FenixUserId("userId");
		SiteId siteId = new SiteId(UUID.randomUUID().toString(), "externalId");
		ProjectId projectId = new ProjectId(UUID.randomUUID());
		UserAddition userAddition = UserAddition.builder()
			.siteId(siteId)
			.projectId(projectId)
			.userId("userId")
			.correlationId(correlationId)
			.userId(fenixUserId.id)
			.status(UserStatus.ADDED)
			.build();
		FURMSUser user = FURMSUser.builder()
			.email("admin@admin.pl")
			.build();
		when(usersDAO.findById(fenixUserId)).thenReturn(Optional.of(user));
		when(repository.findAdditionStatusByCorrelationId(correlationId)).thenReturn(Optional.of(userStatus));
		when(repository.findAdditionStatusByCorrelationId(correlationId)).thenReturn(Optional.of(userStatus));
		when(repository.findAdditionByCorrelationId(correlationId)).thenReturn(userAddition);
		GrantAccess grantAccess = GrantAccess.builder().build();
		when(resourceAccessRepository.findWaitingGrantAccesses(new FenixUserId("userId"), projectId, siteId))
			.thenReturn(Set.of(grantAccess));

		service.update(userAddition);

		orderVerifier.verify(repository).update(any(UserAddition.class));
		orderVerifier.verify(resourceAccessRepository).update(any(), eq(grantAccess), eq(AccessStatus.GRANT_PENDING));
		orderVerifier.verify(siteAgentResourceAccessService).grantAccess(any(), eq(grantAccess), eq(user));
	}

	@ParameterizedTest
	@EnumSource(value = UserStatus.class, names = {"ADDING_PENDING", "ADDING_ACKNOWLEDGED"})
	void shouldUpdateUserAdditionToFailed(UserStatus userStatus) {
		CorrelationId correlationId = CorrelationId.randomID();
		SiteId siteId = new SiteId(UUID.randomUUID().toString(), "externalId");
		ProjectId projectId = new ProjectId(UUID.randomUUID());
		UserAddition userAddition = UserAddition.builder()
			.siteId(siteId)
			.projectId(projectId)
			.userId("userId")
			.correlationId(correlationId)
			.status(UserStatus.ADDED)
			.build();
		when(repository.findAdditionStatusByCorrelationId(correlationId)).thenReturn(Optional.of(userStatus));
		when(repository.findAdditionByCorrelationId(correlationId)).thenReturn(userAddition);

		service.update(userAddition);

		orderVerifier.verify(repository).update(any(UserAddition.class));
	}

	@ParameterizedTest
	@EnumSource(value = UserStatus.class, names = {"ADDING_PENDING", "ADDING_ACKNOWLEDGED", "REMOVED"}, mode = EXCLUDE)
	void shouldNotUpdateUserAdditionToFailed(UserStatus userStatus) {
		CorrelationId correlationId = CorrelationId.randomID();
		when(repository.findAdditionStatusByCorrelationId(correlationId)).thenReturn(Optional.of(userStatus));

		UserAddition userAddition = UserAddition.builder()
			.correlationId(correlationId)
			.status(UserStatus.ADDING_FAILED)
			.build();

		assertThrows(IllegalArgumentException.class, () -> service.update(userAddition));
	}

	@ParameterizedTest
	@EnumSource(value = UserStatus.class, names = {"ADDING_PENDING", "ADDING_ACKNOWLEDGED"}, mode = EXCLUDE)
	void shouldNotUpdateUserAdditionToAdded(UserStatus userStatus) {
		CorrelationId correlationId = CorrelationId.randomID();
		when(repository.findAdditionStatusByCorrelationId(correlationId)).thenReturn(Optional.of(userStatus));

		UserAddition userAddition = UserAddition.builder()
			.correlationId(correlationId)
			.status(UserStatus.ADDED)
			.build();

		assertThrows(IllegalArgumentException.class, () -> service.update(userAddition));
	}

	@Test
	void shouldUpdateUserAdditionToAcknowledged() {
		CorrelationId correlationId = CorrelationId.randomID();
		when(repository.findAdditionStatusByCorrelationId(correlationId)).thenReturn(Optional.of(ADDING_PENDING));

		service.update(UserAddition.builder()
			.correlationId(correlationId)
			.status(UserStatus.ADDING_ACKNOWLEDGED)
			.build());

		orderVerifier.verify(repository).update(any(UserAddition.class));
	}

	@ParameterizedTest
	@EnumSource(value = UserStatus.class, names = {"REMOVAL_PENDING", "REMOVAL_ACKNOWLEDGED"})
	void shouldRemoveUserAddition(UserStatus userStatus) {
		CorrelationId correlationId = CorrelationId.randomID();
		SiteId siteId = new SiteId(UUID.randomUUID().toString(), "externalId");
		ProjectId projectId = new ProjectId(UUID.randomUUID());
		when(repository.findAdditionStatusByCorrelationId(correlationId)).thenReturn(Optional.of(userStatus));
		when(repository.findAdditionByCorrelationId(correlationId)).thenReturn(UserAddition.builder()
			.siteId(siteId)
			.correlationId(correlationId)
			.userId("id")
			.projectId(projectId)
			.status(userStatus)
			.build());

		service.updateStatus(correlationId, REMOVED, Optional.empty());

		orderVerifier.verify(repository).deleteByCorrelationId(correlationId);
		orderVerifier.verify(resourceAccessRepository).deleteByUserAndSiteIdAndProjectId(new FenixUserId("id"), siteId,
			projectId);
	}

	@ParameterizedTest
	@EnumSource(value = UserStatus.class, names = {"REMOVAL_PENDING", "REMOVAL_ACKNOWLEDGED"}, mode = EXCLUDE)
	void shouldNotRemoveUserAddition(UserStatus userStatus) {
		CorrelationId correlationId = CorrelationId.randomID();
		when(repository.findAdditionStatusByCorrelationId(correlationId)).thenReturn(Optional.of(userStatus));

		assertThrows(IllegalArgumentException.class, () -> service.updateStatus(correlationId, REMOVED, Optional.empty()));
	}

	@ParameterizedTest
	@EnumSource(value = UserStatus.class, names = {"ADDING_PENDING"}, mode = EXCLUDE)
	void shouldNotUpdateUserAdditionToAcknowledged(UserStatus userStatus) {
		CorrelationId correlationId = CorrelationId.randomID();
		when(repository.findAdditionStatusByCorrelationId(correlationId)).thenReturn(Optional.of(userStatus));

		UserAddition userAddition = UserAddition.builder()
			.correlationId(correlationId)
			.status(ADDING_ACKNOWLEDGED)
			.build();

		assertThrows(IllegalArgumentException.class, () -> service.update(userAddition));
	}

	@Test
	void shouldUpdateUserAdditionStatus() {
		CorrelationId correlationId = CorrelationId.randomID();

		when(repository.findAdditionStatusByCorrelationId(correlationId)).thenReturn(Optional.of(ADDING_PENDING));
		service.updateStatus(correlationId, ADDING_ACKNOWLEDGED, Optional.empty());

		orderVerifier.verify(repository).updateStatus(correlationId, ADDING_ACKNOWLEDGED, Optional.empty());
	}
}