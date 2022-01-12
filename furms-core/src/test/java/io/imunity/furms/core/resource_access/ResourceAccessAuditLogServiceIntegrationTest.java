/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.core.resource_access;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.imunity.furms.api.authz.AuthzService;
import io.imunity.furms.api.resource_access.ResourceAccessService;
import io.imunity.furms.core.policy_documents.PolicyNotificationService;
import io.imunity.furms.core.user_site_access.UserSiteAccessInnerService;
import io.imunity.furms.domain.resource_access.GrantAccess;
import io.imunity.furms.domain.resource_access.ProjectUserGrant;
import io.imunity.furms.domain.site_agent.CorrelationId;
import io.imunity.furms.domain.sites.SiteId;
import io.imunity.furms.domain.user_operation.UserStatus;
import io.imunity.furms.domain.users.FURMSUser;
import io.imunity.furms.domain.users.FenixUserId;
import io.imunity.furms.site.api.site_agent.SiteAgentResourceAccessService;
import io.imunity.furms.spi.audit_log.AuditLogRepository;
import io.imunity.furms.spi.resource_access.ResourceAccessRepository;
import io.imunity.furms.spi.user_operation.UserOperationRepository;
import io.imunity.furms.spi.users.UsersDAO;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.Optional;
import java.util.UUID;

import static io.imunity.furms.domain.resource_access.AccessStatus.GRANT_PENDING;
import static io.imunity.furms.domain.resource_access.AccessStatus.REVOKED;
import static io.imunity.furms.domain.resource_access.AccessStatus.REVOKE_PENDING;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@SpringBootTest
@SpringBootApplication(scanBasePackages = "io.imunity.furms.core.audit_log", scanBasePackageClasses = ResourceAccessAuditLogService.class)
class ResourceAccessAuditLogServiceIntegrationTest {
	@MockBean
	private SiteAgentResourceAccessService siteAgentResourceAccessService;
	@MockBean
	private ResourceAccessRepository repository;
	@MockBean
	private UserOperationRepository userRepository;
	@MockBean
	private PolicyNotificationService policyNotificationService;
	@MockBean
	private UserSiteAccessInnerService userSiteAccessInnerService;
	@MockBean
	private UsersDAO usersDAO;

	@MockBean
	private AuthzService authzService;
	@MockBean
	private ObjectMapper objectMapper;
	@Autowired
	private ApplicationEventPublisher publisher;
	@MockBean
	private AuditLogRepository auditLogRepository;

	private ResourceAccessService service;
	private UserAllocationStatusUpdaterImpl updater;

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
		service = new ResourceAccessServiceImpl(siteAgentResourceAccessService, repository, userRepository, authzService, userSiteAccessInnerService, policyNotificationService, publisher);
		updater = new UserAllocationStatusUpdaterImpl(repository, userSiteAccessInnerService, publisher);
	}

	@Test
	void shouldDetectGrantAccess() {
		//given
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
		when(usersDAO.findById(fenixUserId)).thenReturn(Optional.of(
			FURMSUser.builder()
				.fenixUserId(fenixUserId)
				.email("email")
				.build()
		));

		service.grantAccess(grantAccess);
		for (TransactionSynchronization transactionSynchronization : TransactionSynchronizationManager
			.getSynchronizations()) {
			transactionSynchronization.afterCommit();
		}

		Mockito.verify(auditLogRepository).create(any());
	}

	@Test
	void shouldDetectRevokeAccess() {
		CorrelationId correlationId = CorrelationId.randomID();

		when(repository.findCurrentStatus(correlationId)).thenReturn(Optional.of(REVOKE_PENDING));
		FenixUserId userId = new FenixUserId("userId");
		when(repository.findUsersGrantsByCorrelationId(correlationId)).thenReturn(Optional.of(new ProjectUserGrant("siteId","grantId","projectId", userId)));
		when(usersDAO.findById(userId)).thenReturn(Optional.of(
			FURMSUser.builder()
				.fenixUserId(userId)
				.email("email")
				.build()
		));

		updater.update(correlationId, REVOKED, "msg");
		for (TransactionSynchronization transactionSynchronization : TransactionSynchronizationManager
			.getSynchronizations()) {
			transactionSynchronization.afterCommit();
		}

		Mockito.verify(auditLogRepository).create(any());
	}
}
