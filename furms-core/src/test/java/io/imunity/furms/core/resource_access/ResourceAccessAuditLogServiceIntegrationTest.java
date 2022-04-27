/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.core.resource_access;

import io.imunity.furms.api.resource_access.ResourceAccessService;
import io.imunity.furms.core.MockedTransactionManager;
import io.imunity.furms.domain.audit_log.Action;
import io.imunity.furms.domain.audit_log.AuditLog;
import io.imunity.furms.domain.audit_log.Operation;
import io.imunity.furms.domain.project_allocation.ProjectAllocationId;
import io.imunity.furms.domain.projects.ProjectId;
import io.imunity.furms.domain.resource_access.GrantAccess;
import io.imunity.furms.domain.resource_access.GrantId;
import io.imunity.furms.domain.sites.SiteId;
import io.imunity.furms.domain.user_operation.UserStatus;
import io.imunity.furms.domain.users.FURMSUser;
import io.imunity.furms.domain.users.FenixUserId;
import io.imunity.furms.domain.users.PersistentId;
import io.imunity.furms.spi.audit_log.AuditLogRepository;
import io.imunity.furms.spi.resource_access.ResourceAccessRepository;
import io.imunity.furms.spi.user_operation.UserOperationRepository;
import io.imunity.furms.spi.users.UsersDAO;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.Optional;
import java.util.UUID;

import static io.imunity.furms.domain.resource_access.AccessStatus.GRANTED;
import static io.imunity.furms.domain.resource_access.AccessStatus.GRANT_PENDING;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@SpringBootTest(classes = {ResourceAccessAuditLogServiceLauncher.class, Config.class})
class ResourceAccessAuditLogServiceIntegrationTest {

	@Autowired
	private ResourceAccessRepository repository;
	@Autowired
	private UserOperationRepository userRepository;
	@Autowired
	private UsersDAO usersDAO;
	@Autowired
	private ApplicationEventPublisher publisher;
	@Autowired
	private AuditLogRepository auditLogRepository;
	@Autowired
	private MockedTransactionManager mockedTransactionManager;

	@Autowired
	private ResourceAccessService service;

	@BeforeEach
	void setUp() {
		TransactionSynchronizationManager.initSynchronization();
	}

	@AfterEach
	void clear() {
		TransactionSynchronizationManager.clear();
	}

	@Test
	void shouldDetectGrantAccess() {
		//given
		GrantId grantId = new GrantId(UUID.randomUUID());
		FenixUserId fenixUserId = new FenixUserId("userId");
		SiteId siteId = new SiteId(UUID.randomUUID().toString(), "externalId");
		ProjectId projectId = new ProjectId(UUID.randomUUID());
		GrantAccess grantAccess = GrantAccess.builder()
			.siteId(siteId)
			.projectId(projectId)
			.fenixUserId(fenixUserId)
			.build();
		//when
		when(repository.create(any(), eq(grantAccess), eq(GRANT_PENDING))).thenReturn(grantId);
		when(repository.exists(grantAccess)).thenReturn(false);
		when(userRepository.findAdditionStatus(siteId, projectId, fenixUserId)).thenReturn(Optional.of(UserStatus.ADDED));
		when(usersDAO.findById(fenixUserId)).thenReturn(Optional.of(
			FURMSUser.builder()
				.id(new PersistentId("id"))
				.fenixUserId(fenixUserId)
				.email("email")
				.build()
		));

		service.grantAccess(grantAccess);

		ArgumentCaptor<AuditLog> argument = ArgumentCaptor.forClass(AuditLog.class);
		Mockito.verify(auditLogRepository).create(argument.capture());
		assertEquals(Operation.PROJECT_RESOURCE_ASSIGNMENT, argument.getValue().operationCategory);
		assertEquals(Action.GRANT, argument.getValue().action);
	}

	@Test
	void shouldDetectRevokeAccess() {
		FenixUserId userId = new FenixUserId("userId");
		SiteId siteId = new SiteId(UUID.randomUUID().toString(), "externalId");
		ProjectId projectId = new ProjectId(UUID.randomUUID());
		ProjectAllocationId allocationId = new ProjectAllocationId(UUID.randomUUID());
		GrantAccess grantAccess = GrantAccess.builder()
			.siteId(siteId)
			.projectId(projectId)
			.allocationId(allocationId)
			.fenixUserId(userId)
			.build();

		when(repository.findCurrentStatus(userId, allocationId)).thenReturn(GRANTED);
		when(usersDAO.findById(userId)).thenReturn(Optional.of(
			FURMSUser.builder()
				.id(new PersistentId("id"))
				.fenixUserId(userId)
				.email("email")
				.build()
		));

		service.revokeAccess(grantAccess);

		ArgumentCaptor<AuditLog> argument = ArgumentCaptor.forClass(AuditLog.class);
		Mockito.verify(auditLogRepository).create(argument.capture());
		assertEquals(Operation.PROJECT_RESOURCE_ASSIGNMENT, argument.getValue().operationCategory);
		assertEquals(Action.REVOKE, argument.getValue().action);
	}
}
