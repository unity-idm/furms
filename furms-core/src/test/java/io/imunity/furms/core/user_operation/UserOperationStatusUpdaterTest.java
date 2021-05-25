/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.core.user_operation;

import io.imunity.furms.domain.site_agent.CorrelationId;
import io.imunity.furms.domain.user_operation.UserAddition;
import io.imunity.furms.domain.user_operation.UserStatus;
import io.imunity.furms.domain.users.FenixUserId;
import io.imunity.furms.spi.resource_access.ResourceAccessRepository;
import io.imunity.furms.spi.user_operation.UserOperationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;

import static io.imunity.furms.domain.user_operation.UserStatus.*;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.params.provider.EnumSource.Mode.EXCLUDE;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class UserOperationStatusUpdaterTest {
	@Mock
	private UserOperationRepository repository;
	@Mock
	private ResourceAccessRepository resourceAccessRepository;

	private UserOperationStatusUpdaterImpl service;
	private InOrder orderVerifier;

	@BeforeEach
	void init() {
		MockitoAnnotations.initMocks(this);
		service = new UserOperationStatusUpdaterImpl(repository, resourceAccessRepository);
		orderVerifier = inOrder(repository, resourceAccessRepository);
	}

	@ParameterizedTest
	@EnumSource(value = UserStatus.class, names = {"ADDING_PENDING", "ADDING_ACKNOWLEDGED"})
	void shouldUpdateUserAdditionToAdded(UserStatus userStatus) {
		CorrelationId correlationId = CorrelationId.randomID();
		when(repository.findAdditionStatusByCorrelationId(correlationId.id)).thenReturn(userStatus);

		service.update(UserAddition.builder()
			.correlationId(correlationId)
			.status(UserStatus.ADDED)
			.build());

		orderVerifier.verify(repository).update(any(UserAddition.class));
	}

	@ParameterizedTest
	@EnumSource(value = UserStatus.class, names = {"ADDING_PENDING", "ADDING_ACKNOWLEDGED"})
	void shouldUpdateUserAdditionToFailed(UserStatus userStatus) {
		CorrelationId correlationId = CorrelationId.randomID();
		when(repository.findAdditionStatusByCorrelationId(correlationId.id)).thenReturn(userStatus);

		service.update(UserAddition.builder()
			.correlationId(correlationId)
			.status(UserStatus.ADDED)
			.build());

		orderVerifier.verify(repository).update(any(UserAddition.class));
	}

	@ParameterizedTest
	@EnumSource(value = UserStatus.class, names = {"ADDING_PENDING", "ADDING_ACKNOWLEDGED", "REMOVED"}, mode = EXCLUDE)
	void shouldNotUpdateUserAdditionToFailed(UserStatus userStatus) {
		CorrelationId correlationId = CorrelationId.randomID();
		when(repository.findAdditionStatusByCorrelationId(correlationId.id)).thenReturn(userStatus);

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
		when(repository.findAdditionStatusByCorrelationId(correlationId.id)).thenReturn(userStatus);

		UserAddition userAddition = UserAddition.builder()
			.correlationId(correlationId)
			.status(UserStatus.ADDED)
			.build();

		assertThrows(IllegalArgumentException.class, () -> service.update(userAddition));
	}

	@Test
	void shouldUpdateUserAdditionToAcknowledged() {
		CorrelationId correlationId = CorrelationId.randomID();
		when(repository.findAdditionStatusByCorrelationId(correlationId.id)).thenReturn(ADDING_PENDING);

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
		when(repository.findAdditionStatusByCorrelationId(correlationId.id)).thenReturn(userStatus);
		when(repository.findAdditionByCorrelationId(correlationId)).thenReturn(UserAddition.builder()
			.correlationId(correlationId)
			.userId("id")
			.projectId("projectId")
			.status(userStatus)
			.build());

		service.updateStatus(correlationId, REMOVED, Optional.empty());

		orderVerifier.verify(repository).deleteByCorrelationId(correlationId.id);
		orderVerifier.verify(resourceAccessRepository).deleteByUserAndProjectId(new FenixUserId("id"), "projectId");
	}

	@ParameterizedTest
	@EnumSource(value = UserStatus.class, names = {"REMOVAL_PENDING", "REMOVAL_ACKNOWLEDGED"}, mode = EXCLUDE)
	void shouldNotRemoveUserAddition(UserStatus userStatus) {
		CorrelationId correlationId = CorrelationId.randomID();
		when(repository.findAdditionStatusByCorrelationId(correlationId.id)).thenReturn(userStatus);

		UserAddition userAddition = UserAddition.builder()
			.correlationId(correlationId)
			.userId("id")
			.projectId("projectId")
			.status(REMOVED)
			.build();

		assertThrows(IllegalArgumentException.class, () -> service.updateStatus(correlationId, REMOVED, Optional.empty()));
	}

	@ParameterizedTest
	@EnumSource(value = UserStatus.class, names = {"ADDING_PENDING"}, mode = EXCLUDE)
	void shouldNotUpdateUserAdditionToAcknowledged(UserStatus userStatus) {
		CorrelationId correlationId = CorrelationId.randomID();
		when(repository.findAdditionStatusByCorrelationId(correlationId.id)).thenReturn(userStatus);

		UserAddition userAddition = UserAddition.builder()
			.correlationId(correlationId)
			.status(ADDING_ACKNOWLEDGED)
			.build();

		assertThrows(IllegalArgumentException.class, () -> service.update(userAddition));
	}

	@Test
	void shouldUpdateUserAdditionStatus() {
		CorrelationId correlationId = CorrelationId.randomID();

		when(repository.findAdditionStatusByCorrelationId(correlationId.id)).thenReturn(ADDING_PENDING);
		service.updateStatus(correlationId, ADDING_ACKNOWLEDGED, Optional.empty());

		orderVerifier.verify(repository).updateStatus(correlationId, ADDING_ACKNOWLEDGED, Optional.empty());
	}
}