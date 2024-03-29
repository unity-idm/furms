/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.core.resource_access;

import io.imunity.furms.core.post_commit.PostCommitRunner;
import io.imunity.furms.core.user_site_access.UserSiteAccessInnerService;
import io.imunity.furms.domain.projects.ProjectId;
import io.imunity.furms.domain.resource_access.AccessStatus;
import io.imunity.furms.domain.resource_access.GrantAccess;
import io.imunity.furms.domain.resource_access.GrantId;
import io.imunity.furms.domain.resource_access.ProjectUserGrant;
import io.imunity.furms.domain.site_agent.CorrelationId;
import io.imunity.furms.domain.sites.SiteId;
import io.imunity.furms.domain.users.FenixUserId;
import io.imunity.furms.spi.resource_access.ResourceAccessRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.Optional;
import java.util.UUID;

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

@ExtendWith(MockitoExtension.class)
class UserAllocationStatusUpdaterTest {
	@Mock
	private ResourceAccessRepository repository;
	@Mock
	private PostCommitRunner postCommitRunner;
	@Mock
	private UserSiteAccessInnerService userSiteAccessInnerService;

	private UserAllocationStatusUpdaterImpl service;
	private InOrder orderVerifier;

	@BeforeEach
	void init() {
		service = new UserAllocationStatusUpdaterImpl(repository, userSiteAccessInnerService, postCommitRunner);
		orderVerifier = inOrder(repository, postCommitRunner, userSiteAccessInnerService);
		TransactionSynchronizationManager.initSynchronization();
	}

	@AfterEach
	void clean() {
		TransactionSynchronizationManager.clear();
	}

	@ParameterizedTest
	@EnumSource(value = AccessStatus.class, names = {"GRANT_PENDING", "GRANT_ACKNOWLEDGED"})
	void shouldUpdateUsersGrantToGrand(AccessStatus status) {
		CorrelationId correlationId = CorrelationId.randomID();

		when(repository.findCurrentStatus(correlationId)).thenReturn(Optional.of(status));

		service.update(correlationId, GRANTED, "msg");

		orderVerifier.verify(repository).update(correlationId, GRANTED, "msg");
	}

	@Test
	void shouldUpdateUsersGrantToAcknowledged() {
		CorrelationId correlationId = CorrelationId.randomID();

		when(repository.findCurrentStatus(correlationId)).thenReturn(Optional.of(GRANT_PENDING));
		service.update(correlationId, GRANT_ACKNOWLEDGED, "msg");

		orderVerifier.verify(repository).update(correlationId, GRANT_ACKNOWLEDGED, "msg");
	}

	@ParameterizedTest
	@EnumSource(value = AccessStatus.class, names = {"GRANT_PENDING", "GRANT_ACKNOWLEDGED"})
	void shouldUpdateUsersGrantToFailed(AccessStatus status) {
		CorrelationId correlationId = CorrelationId.randomID();

		when(repository.findCurrentStatus(correlationId)).thenReturn(Optional.of(status));
		service.update(correlationId, GRANT_FAILED, "msg");

		orderVerifier.verify(repository).update(correlationId, GRANT_FAILED, "msg");
	}

	@ParameterizedTest
	@EnumSource(value = AccessStatus.class, names = {"GRANT_PENDING", "GRANT_ACKNOWLEDGED", "REVOKED"}, mode = EXCLUDE)
	void shouldNotUpdateUsersGrantToGrantedIfPreviousStateIs(AccessStatus status) {
		CorrelationId correlationId = CorrelationId.randomID();

		when(repository.findCurrentStatus(correlationId)).thenReturn(Optional.of(status));

		assertThrows(IllegalArgumentException.class, () -> service.update(correlationId, GRANTED, "msg"));
	}

	@ParameterizedTest
	@EnumSource(value = AccessStatus.class, names = {"GRANT_PENDING", "REVOKED"}, mode = EXCLUDE)
	void shouldNotUpdateUsersGrantToAcknowledgedIfPreviousStateIs(AccessStatus status) {
		CorrelationId correlationId = CorrelationId.randomID();

		when(repository.findCurrentStatus(correlationId)).thenReturn(Optional.of(status));

		assertThrows(IllegalArgumentException.class, () -> service.update(correlationId, GRANT_ACKNOWLEDGED, "msg"));
	}

	@ParameterizedTest
	@EnumSource(value = AccessStatus.class, names = {"GRANT_PENDING", "GRANT_ACKNOWLEDGED", "REVOKED"}, mode = EXCLUDE)
	void shouldNotUpdateUsersGrantToFailedIfPreviousStateIs(AccessStatus status) {
		CorrelationId correlationId = CorrelationId.randomID();

		when(repository.findCurrentStatus(correlationId)).thenReturn(Optional.of(status));

		assertThrows(IllegalArgumentException.class, () -> service.update(correlationId, GRANTED, "msg"));
	}

	@Test
	void shouldUpdateUsersRevokeToAcknowledged() {
		CorrelationId correlationId = CorrelationId.randomID();

		when(repository.findCurrentStatus(correlationId)).thenReturn(Optional.of(REVOKE_PENDING));
		service.update(correlationId, REVOKE_ACKNOWLEDGED, "msg");

		orderVerifier.verify(repository).update(correlationId, REVOKE_ACKNOWLEDGED, "msg");
	}

	@ParameterizedTest
	@EnumSource(value = AccessStatus.class, names = {"REVOKE_PENDING", "REVOKE_ACKNOWLEDGED"})
	void shouldUpdateUsersRevokeToFailed(AccessStatus status) {
		CorrelationId correlationId = CorrelationId.randomID();

		when(repository.findCurrentStatus(correlationId)).thenReturn(Optional.of(status));
		service.update(correlationId, REVOKE_FAILED, "msg");

		orderVerifier.verify(repository).update(correlationId, REVOKE_FAILED, "msg");
	}

	@ParameterizedTest
	@EnumSource(value = AccessStatus.class, names = {"REVOKE_PENDING"}, mode = EXCLUDE)
	void shouldNotUpdateUsersRevokeToAcknowledgedIfPreviousStateIs(AccessStatus status) {
		CorrelationId correlationId = CorrelationId.randomID();

		when(repository.findCurrentStatus(correlationId)).thenReturn(Optional.of(status));

		assertThrows(IllegalArgumentException.class, () -> service.update(correlationId, REVOKE_ACKNOWLEDGED, "msg"));
	}

	@ParameterizedTest
	@EnumSource(value = AccessStatus.class, names = {"REVOKE_PENDING", "REVOKE_ACKNOWLEDGED", "REVOKED"}, mode = EXCLUDE)
	void shouldNotUpdateUsersRevokeToFailedIfPreviousStateIs(AccessStatus status) {
		CorrelationId correlationId = CorrelationId.randomID();

		when(repository.findCurrentStatus(correlationId)).thenReturn(Optional.of(status));

		assertThrows(IllegalArgumentException.class, () -> service.update(correlationId, REVOKE_FAILED, "msg"));
	}

	@ParameterizedTest
	@EnumSource(value = AccessStatus.class, names = {"REVOKE_PENDING", "REVOKE_ACKNOWLEDGED"})
	void shouldRemoveUsersGrant(AccessStatus status) {
		CorrelationId correlationId = CorrelationId.randomID();
		SiteId siteId = new SiteId(UUID.randomUUID());
		GrantId grantId = new GrantId(UUID.randomUUID());
		ProjectId projectId = new ProjectId(UUID.randomUUID());
		when(repository.findCurrentStatus(correlationId)).thenReturn(Optional.of(status));
		when(repository.findUsersGrantsByCorrelationId(correlationId)).thenReturn(Optional.of(new ProjectUserGrant(siteId,grantId,projectId, new FenixUserId("userId"))));
		service.update(correlationId, REVOKED, "msg");

		orderVerifier.verify(repository).deleteByCorrelationId(correlationId);
		verify(repository, times(0)).update(correlationId, REVOKED, "msg");
	}

	@ParameterizedTest
	@EnumSource(value = AccessStatus.class, names = {"REVOKE_PENDING", "REVOKE_ACKNOWLEDGED"})
	void shouldRemoveUsersGrantAndUserAdditionWhenItIsLastGrant(AccessStatus status) {
		CorrelationId correlationId = CorrelationId.randomID();
		FenixUserId fenixUserId = new FenixUserId("userId");
		SiteId siteId = new SiteId(UUID.randomUUID());
		GrantId grantId = new GrantId(UUID.randomUUID());
		ProjectId projectId = new ProjectId(UUID.randomUUID());

		when(repository.findCurrentStatus(correlationId)).thenReturn(Optional.of(status));
		when(repository.findUsersGrantsByCorrelationId(correlationId)).thenReturn(Optional.of(new ProjectUserGrant(siteId, grantId, projectId,
			new FenixUserId("userId"))));

		service.update(correlationId, REVOKED, "msg");

		orderVerifier.verify(repository).deleteByCorrelationId(correlationId);
		orderVerifier.verify(userSiteAccessInnerService).revokeAccessToSite(GrantAccess.builder()
				.siteId(siteId)
				.projectId(projectId)
				.fenixUserId(fenixUserId)
				.build()
		);
		verify(repository, times(0)).update(correlationId, REVOKED, "msg");
	}

	@ParameterizedTest
	@EnumSource(value = AccessStatus.class, names = {"REVOKE_PENDING", "REVOKE_ACKNOWLEDGED"}, mode = EXCLUDE)
	void shouldNotRemoveUsersGrantIfPreviousStateIs(AccessStatus status) {
		CorrelationId correlationId = CorrelationId.randomID();

		when(repository.findCurrentStatus(correlationId)).thenReturn(Optional.of(status));

		assertThrows(IllegalArgumentException.class, () -> service.update(correlationId, REVOKED, "msg"));
	}
}