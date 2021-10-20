/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.core.resource_access;

import io.imunity.furms.core.user_operation.UserOperationService;
import io.imunity.furms.domain.resource_access.AccessStatus;
import io.imunity.furms.domain.resource_access.ProjectUserGrant;
import io.imunity.furms.domain.site_agent.CorrelationId;
import io.imunity.furms.domain.users.FenixUserId;
import io.imunity.furms.spi.resource_access.ResourceAccessRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;
import java.util.Set;

import static io.imunity.furms.domain.resource_access.AccessStatus.GRANTED;
import static io.imunity.furms.domain.resource_access.AccessStatus.GRANT_ACKNOWLEDGED;
import static io.imunity.furms.domain.resource_access.AccessStatus.GRANT_FAILED;
import static io.imunity.furms.domain.resource_access.AccessStatus.GRANT_PENDING;
import static io.imunity.furms.domain.resource_access.AccessStatus.REVOKED;
import static io.imunity.furms.domain.resource_access.AccessStatus.REVOKE_ACKNOWLEDGED;
import static io.imunity.furms.domain.resource_access.AccessStatus.REVOKE_FAILED;
import static io.imunity.furms.domain.resource_access.AccessStatus.REVOKE_PENDING;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.params.provider.EnumSource.Mode.EXCLUDE;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class UserAllocationStatusUpdaterTest {
	@Mock
	private ResourceAccessRepository repository;
	@Mock
	private UserOperationService userOperationService;

	private UserAllocationStatusUpdaterImpl service;
	private InOrder orderVerifier;

	@BeforeEach
	void init() {
		MockitoAnnotations.initMocks(this);
		service = new UserAllocationStatusUpdaterImpl(repository, userOperationService);
		orderVerifier = inOrder(repository, userOperationService);
	}

	@ParameterizedTest
	@EnumSource(value = AccessStatus.class, names = {"GRANT_PENDING", "GRANT_ACKNOWLEDGED"})
	void shouldUpdateUsersGrantToGrand(AccessStatus status) {
		CorrelationId correlationId = CorrelationId.randomID();

		FenixUserId userId = new FenixUserId("userId");
		when(repository.findUsersGrantsByCorrelationId(correlationId))
			.thenReturn(Optional.of(new ProjectUserGrant("siteId", "grantId", "projectId", userId)));
		when(repository.findCurrentStatus(correlationId)).thenReturn(status);
		service.update(correlationId, GRANTED, "msg");

		orderVerifier.verify(repository).update(correlationId, GRANTED, "msg");
	}

	@Test
	void shouldUpdateUsersGrantToAcknowledged() {
		CorrelationId correlationId = CorrelationId.randomID();

		when(repository.findCurrentStatus(correlationId)).thenReturn(GRANT_PENDING);
		service.update(correlationId, GRANT_ACKNOWLEDGED, "msg");

		orderVerifier.verify(repository).update(correlationId, GRANT_ACKNOWLEDGED, "msg");
	}

	@ParameterizedTest
	@EnumSource(value = AccessStatus.class, names = {"GRANT_PENDING", "GRANT_ACKNOWLEDGED"})
	void shouldUpdateUsersGrantToFailed(AccessStatus status) {
		CorrelationId correlationId = CorrelationId.randomID();

		when(repository.findCurrentStatus(correlationId)).thenReturn(status);
		service.update(correlationId, GRANT_FAILED, "msg");

		orderVerifier.verify(repository).update(correlationId, GRANT_FAILED, "msg");
	}

	@ParameterizedTest
	@EnumSource(value = AccessStatus.class, names = {"GRANT_PENDING", "GRANT_ACKNOWLEDGED", "REVOKED"}, mode = EXCLUDE)
	void shouldNotUpdateUsersGrantToGrantedIfPreviousStateIs(AccessStatus status) {
		CorrelationId correlationId = CorrelationId.randomID();

		when(repository.findCurrentStatus(correlationId)).thenReturn(status);

		assertThrows(IllegalArgumentException.class, () -> service.update(correlationId, GRANTED, "msg"));
	}

	@ParameterizedTest
	@EnumSource(value = AccessStatus.class, names = {"GRANT_PENDING", "REVOKED"}, mode = EXCLUDE)
	void shouldNotUpdateUsersGrantToAcknowledgedIfPreviousStateIs(AccessStatus status) {
		CorrelationId correlationId = CorrelationId.randomID();

		when(repository.findCurrentStatus(correlationId)).thenReturn(status);

		assertThrows(IllegalArgumentException.class, () -> service.update(correlationId, GRANT_ACKNOWLEDGED, "msg"));
	}

	@ParameterizedTest
	@EnumSource(value = AccessStatus.class, names = {"GRANT_PENDING", "GRANT_ACKNOWLEDGED", "REVOKED"}, mode = EXCLUDE)
	void shouldNotUpdateUsersGrantToFailedIfPreviousStateIs(AccessStatus status) {
		CorrelationId correlationId = CorrelationId.randomID();

		when(repository.findCurrentStatus(correlationId)).thenReturn(status);

		assertThrows(IllegalArgumentException.class, () -> service.update(correlationId, GRANTED, "msg"));
	}

	@Test
	void shouldUpdateUsersRevokeToAcknowledged() {
		CorrelationId correlationId = CorrelationId.randomID();

		when(repository.findCurrentStatus(correlationId)).thenReturn(REVOKE_PENDING);
		service.update(correlationId, REVOKE_ACKNOWLEDGED, "msg");

		orderVerifier.verify(repository).update(correlationId, REVOKE_ACKNOWLEDGED, "msg");
	}

	@ParameterizedTest
	@EnumSource(value = AccessStatus.class, names = {"REVOKE_PENDING", "REVOKE_ACKNOWLEDGED"})
	void shouldUpdateUsersRevokeToFailed(AccessStatus status) {
		CorrelationId correlationId = CorrelationId.randomID();

		when(repository.findCurrentStatus(correlationId)).thenReturn(status);
		service.update(correlationId, REVOKE_FAILED, "msg");

		orderVerifier.verify(repository).update(correlationId, REVOKE_FAILED, "msg");
	}

	@ParameterizedTest
	@EnumSource(value = AccessStatus.class, names = {"REVOKE_PENDING"}, mode = EXCLUDE)
	void shouldNotUpdateUsersRevokeToAcknowledgedIfPreviousStateIs(AccessStatus status) {
		CorrelationId correlationId = CorrelationId.randomID();

		when(repository.findCurrentStatus(correlationId)).thenReturn(status);

		assertThrows(IllegalArgumentException.class, () -> service.update(correlationId, REVOKE_ACKNOWLEDGED, "msg"));
	}

	@ParameterizedTest
	@EnumSource(value = AccessStatus.class, names = {"REVOKE_PENDING", "REVOKE_ACKNOWLEDGED", "REVOKED"}, mode = EXCLUDE)
	void shouldNotUpdateUsersRevokeToFailedIfPreviousStateIs(AccessStatus status) {
		CorrelationId correlationId = CorrelationId.randomID();

		when(repository.findCurrentStatus(correlationId)).thenReturn(status);

		assertThrows(IllegalArgumentException.class, () -> service.update(correlationId, REVOKE_FAILED, "msg"));
	}

	@ParameterizedTest
	@EnumSource(value = AccessStatus.class, names = {"REVOKE_PENDING", "REVOKE_ACKNOWLEDGED"})
	void shouldRemoveUsersGrant(AccessStatus status) {
		CorrelationId correlationId = CorrelationId.randomID();

		when(repository.findCurrentStatus(correlationId)).thenReturn(status);
		when(repository.findUsersGrantsByCorrelationId(correlationId)).thenReturn(Optional.of(new ProjectUserGrant("siteId","grantId","projectId", new FenixUserId("userId"))));
		service.update(correlationId, REVOKED, "msg");

		orderVerifier.verify(repository).deleteByCorrelationId(correlationId);
		verify(repository, times(0)).update(correlationId, REVOKED, "msg");
	}

	@ParameterizedTest
	@EnumSource(value = AccessStatus.class, names = {"REVOKE_PENDING", "REVOKE_ACKNOWLEDGED"})
	void shouldRemoveUsersGrantAndUserAdditionWhenItIsLastGrant(AccessStatus status) {
		CorrelationId correlationId = CorrelationId.randomID();

		when(repository.findCurrentStatus(correlationId)).thenReturn(status);
		when(repository.findUsersGrantsByCorrelationId(correlationId)).thenReturn(Optional.of(new ProjectUserGrant("siteId","grantId","projectId", new FenixUserId("userId"))));
		FenixUserId fenixUserId = new FenixUserId("userId");
		when(repository.findUserGrantsByProjectIdAndFenixUserId("projectId", fenixUserId)).thenReturn(Set.of());
		service.update(correlationId, REVOKED, "msg");

		orderVerifier.verify(userOperationService).createUserRemovals("siteId", "projectId", fenixUserId);
		orderVerifier.verify(repository).deleteByCorrelationId(correlationId);
		verify(repository, times(0)).update(correlationId, REVOKED, "msg");
	}

	@ParameterizedTest
	@EnumSource(value = AccessStatus.class, names = {"REVOKE_PENDING", "REVOKE_ACKNOWLEDGED"}, mode = EXCLUDE)
	void shouldNotRemoveUsersGrantIfPreviousStateIs(AccessStatus status) {
		CorrelationId correlationId = CorrelationId.randomID();

		when(repository.findCurrentStatus(correlationId)).thenReturn(status);

		assertThrows(IllegalArgumentException.class, () -> service.update(correlationId, REVOKED, "msg"));
	}
}