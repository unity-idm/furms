/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.core.sites;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.imunity.furms.api.authz.AuthzService;
import io.imunity.furms.api.authz.CapabilityCollector;
import io.imunity.furms.core.invitations.InvitatoryService;
import io.imunity.furms.core.policy_documents.PolicyNotificationService;
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
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.ApplicationEventPublisher;

import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@SpringBootTest
@SpringBootApplication(scanBasePackages = {"io.imunity.furms.core.audit_log", "io.imunity.furms.core.users.audit_log"}, scanBasePackageClasses = SiteAuditLogService.class)
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

		Mockito.verify(auditLogRepository).create(any());
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

		Mockito.verify(auditLogRepository).create(any());
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

		Mockito.verify(auditLogRepository).create(any());
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
		Mockito.verify(auditLogRepository).create(any());
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
		Mockito.verify(auditLogRepository).create(any());
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

		Mockito.verify(auditLogRepository).create(any());
	}
}
