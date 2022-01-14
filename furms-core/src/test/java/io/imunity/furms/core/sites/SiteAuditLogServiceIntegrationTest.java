/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.core.sites;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.imunity.furms.api.authz.AuthzService;
import io.imunity.furms.api.authz.CapabilityCollector;
import io.imunity.furms.core.audit_log.AuditLogServiceImplTest;
import io.imunity.furms.core.invitations.InvitatoryService;
import io.imunity.furms.core.policy_documents.PolicyNotificationService;
import io.imunity.furms.core.users.audit_log.RoleAssignmentAuditLogServiceTest;
import io.imunity.furms.domain.audit_log.Action;
import io.imunity.furms.domain.audit_log.AuditLog;
import io.imunity.furms.domain.audit_log.Operation;
import io.imunity.furms.domain.sites.Site;
import io.imunity.furms.domain.users.FURMSUser;
import io.imunity.furms.domain.users.PersistentId;
import io.imunity.furms.site.api.site_agent.SiteAgentPolicyDocumentService;
import io.imunity.furms.site.api.site_agent.SiteAgentService;
import io.imunity.furms.site.api.site_agent.SiteAgentStatusService;
import io.imunity.furms.spi.audit_log.AuditLogRepository;
import io.imunity.furms.spi.policy_docuemnts.PolicyDocumentRepository;
import io.imunity.furms.spi.sites.SiteGroupDAO;
import io.imunity.furms.spi.sites.SiteRepository;
import io.imunity.furms.spi.user_operation.UserOperationRepository;
import io.imunity.furms.spi.users.UsersDAO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.ApplicationEventPublisher;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@SpringBootTest
@SpringBootApplication(scanBasePackageClasses = {SiteAuditLogService.class, AuditLogServiceImplTest.class, RoleAssignmentAuditLogServiceTest.class})
class SiteAuditLogServiceIntegrationTest {
	@MockBean
	private SiteRepository repository;
	@MockBean
	private SiteGroupDAO webClient;
	@MockBean
	private UsersDAO usersDAO;
	@MockBean
	private SiteAgentService siteAgentService;
	@MockBean
	private SiteAgentStatusService siteAgentStatusService;
	@MockBean
	private UserOperationRepository userOperationRepository;
	@MockBean
	private PolicyDocumentRepository policyDocumentRepository;
	@MockBean
	private SiteAgentPolicyDocumentService siteAgentPolicyDocumentService;
	@MockBean
	private CapabilityCollector capabilityCollector;
	@MockBean
	private PolicyNotificationService policyNotificationService;
	@MockBean
	private InvitatoryService invitatoryService;
	@MockBean
	private SiteServiceValidator validator;

	@MockBean
	private AuthzService authzService;
	@MockBean
	private ObjectMapper objectMapper;
	@Autowired
	private ApplicationEventPublisher publisher;
	@MockBean
	private AuditLogRepository auditLogRepository;

	private SiteServiceImpl service;

	@BeforeEach
	void setUp() {
		service = new SiteServiceImpl(repository, validator, webClient, usersDAO, publisher, authzService,
			siteAgentService, userOperationRepository, policyDocumentRepository,
			siteAgentPolicyDocumentService, capabilityCollector, policyNotificationService, invitatoryService);
	}

	@Test
	void shouldDetectInfraServiceDeletion() {
		//given
		String id = "id";
		when(repository.exists(id)).thenReturn(true);
		Site site = Site.builder().build();
		when(repository.findById(id)).thenReturn(Optional.of(site));

		//when
		service.delete(id);

		ArgumentCaptor<AuditLog> argument = ArgumentCaptor.forClass(AuditLog.class);
		Mockito.verify(auditLogRepository).create(argument.capture());
		assertEquals(Operation.SITES_MANAGEMENT, argument.getValue().operationCategory);
		assertEquals(Action.DELETE, argument.getValue().action);
	}

	@Test
	void shouldDetectInfraServiceUpdate() {
		//given
		Site request = Site.builder()
			.id("id")
			.name("name")
			.build();
		when(repository.exists(request.getId())).thenReturn(true);
		when(repository.isNamePresentIgnoringRecord(request.getName(), request.getId())).thenReturn(false);
		when(repository.update(request)).thenReturn(request.getId());
		when(repository.findById(request.getId())).thenReturn(Optional.of(request));

		//when
		service.update(request);

		ArgumentCaptor<AuditLog> argument = ArgumentCaptor.forClass(AuditLog.class);
		Mockito.verify(auditLogRepository).create(argument.capture());
		assertEquals(Operation.SITES_MANAGEMENT, argument.getValue().operationCategory);
		assertEquals(Action.UPDATE, argument.getValue().action);
	}

	@Test
	void shouldDetectInfraServiceCreation() {
		//given
		Site request = Site.builder()
			.id("id")
			.name("name")
			.build();
		when(repository.isNamePresent(request.getName())).thenReturn(false);
		when(repository.create(eq(request), any())).thenReturn(request.getId());
		when(repository.findById(request.getId())).thenReturn(Optional.of(request));

		//when
		service.create(request);

		ArgumentCaptor<AuditLog> argument = ArgumentCaptor.forClass(AuditLog.class);
		Mockito.verify(auditLogRepository).create(argument.capture());
		assertEquals(Operation.SITES_MANAGEMENT, argument.getValue().operationCategory);
		assertEquals(Action.CREATE, argument.getValue().action);
	}

	@Test
	void shouldDetectAdminAddition() {
		//given
		String siteId = UUID.randomUUID().toString();
		PersistentId userId = new PersistentId("userId");
		Site site = Site.builder()
			.name("name")
			.build();
		when(repository.findById(siteId)).thenReturn(Optional.of(site));
		when(usersDAO.findById(userId)).thenReturn(Optional.of(FURMSUser.builder()
			.id(userId)
			.email("email")
			.build()));

		//when
		service.addAdmin(siteId, userId);

		//then
		ArgumentCaptor<AuditLog> argument = ArgumentCaptor.forClass(AuditLog.class);
		Mockito.verify(auditLogRepository).create(argument.capture());
		assertEquals(Operation.ROLE_ASSIGNMENT, argument.getValue().operationCategory);
		assertEquals(Action.GRANT, argument.getValue().action);
	}

	@Test
	void shouldDetectSupportAddition() {
		//given
		String siteId = UUID.randomUUID().toString();
		PersistentId userId = new PersistentId("userId");
		Site site = Site.builder()
			.name("name")
			.build();
		when(repository.findById(siteId)).thenReturn(Optional.of(site));
		when(usersDAO.findById(userId)).thenReturn(Optional.of(FURMSUser.builder()
			.id(userId)
			.email("email")
			.build())
		);
		//when
		service.addSupport(siteId, userId);

		//then
		ArgumentCaptor<AuditLog> argument = ArgumentCaptor.forClass(AuditLog.class);
		Mockito.verify(auditLogRepository).create(argument.capture());
		assertEquals(Operation.ROLE_ASSIGNMENT, argument.getValue().operationCategory);
		assertEquals(Action.GRANT, argument.getValue().action);
	}

	@Test
	void shouldDetectUserRemoval() {
		//given
		String siteId = UUID.randomUUID().toString();
		PersistentId userId = new PersistentId("userId");

		Site site = Site.builder()
			.name("name")
			.build();
		when(service.findById(siteId)).thenReturn(Optional.of(site));
		when(usersDAO.findById(userId)).thenReturn(Optional.of(FURMSUser.builder()
				.id(userId)
				.email("email")
				.build()));
		//when
		service.removeSiteUser(siteId, userId);

		ArgumentCaptor<AuditLog> argument = ArgumentCaptor.forClass(AuditLog.class);
		Mockito.verify(auditLogRepository).create(argument.capture());
		assertEquals(Operation.ROLE_ASSIGNMENT, argument.getValue().operationCategory);
		assertEquals(Action.REVOKE, argument.getValue().action);
	}
}
