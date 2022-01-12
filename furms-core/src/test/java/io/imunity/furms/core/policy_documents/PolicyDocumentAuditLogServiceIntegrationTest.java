/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.core.policy_documents;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.imunity.furms.api.authz.AuthzService;
import io.imunity.furms.domain.policy_documents.PolicyAcceptance;
import io.imunity.furms.domain.policy_documents.PolicyDocument;
import io.imunity.furms.domain.policy_documents.PolicyId;
import io.imunity.furms.domain.sites.Site;
import io.imunity.furms.domain.users.FURMSUser;
import io.imunity.furms.domain.users.FenixUserId;
import io.imunity.furms.site.api.site_agent.SiteAgentPolicyDocumentService;
import io.imunity.furms.spi.audit_log.AuditLogRepository;
import io.imunity.furms.spi.policy_docuemnts.PolicyDocumentDAO;
import io.imunity.furms.spi.policy_docuemnts.PolicyDocumentRepository;
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
import static org.mockito.Mockito.when;

@SpringBootTest
@SpringBootApplication(scanBasePackages = "io.imunity.furms.core.audit_log", scanBasePackageClasses = PolicyDocumentAuditLogService.class)
class PolicyDocumentAuditLogServiceIntegrationTest {
	@MockBean
	private PolicyDocumentRepository repository;
	@MockBean
	private PolicyDocumentValidator validator;
	@MockBean
	private PolicyDocumentDAO policyDocumentDAO;
	@MockBean
	private PolicyNotificationService policyNotificationService;
	@MockBean
	private SiteAgentPolicyDocumentService siteAgentPolicyDocumentService;
	@MockBean
	private SiteRepository siteRepository;
	@MockBean
	private UsersDAO usersDAO;
	@MockBean
	private UserOperationRepository userRepository;

	@MockBean
	private AuthzService authzService;
	@MockBean
	private ObjectMapper objectMapper;
	@Autowired
	private ApplicationEventPublisher publisher;
	@MockBean
	private AuditLogRepository auditLogRepository;

	private PolicyDocumentServiceImpl service;

	@BeforeEach
	void init() {
		service = new PolicyDocumentServiceImpl(
			authzService, repository, validator, policyDocumentDAO, policyNotificationService,
			siteAgentPolicyDocumentService, siteRepository,
			userRepository, usersDAO, publisher
		);
	}

	@Test
	void shouldDetectPolicyDocumentDeletion() {
		PolicyId policyId = new PolicyId(UUID.randomUUID());
		PolicyDocument policyDocument = PolicyDocument.builder()
			.id(policyId)
			.build();
		when(repository.findById(policyId)).thenReturn(Optional.of(policyDocument));

		service.delete("siteId", policyId);

		Mockito.verify(auditLogRepository).create(any());
	}

	@Test
	void shouldDetectPolicyDocumentUpdate() {
		PolicyId policyId = new PolicyId(UUID.randomUUID());
		PolicyDocument policyDocument = PolicyDocument.builder()
			.id(policyId)
			.build();
		when(repository.findById(policyId)).thenReturn(Optional.of(policyDocument));
		when(repository.update(policyDocument, false)).thenReturn(policyId);

		service.update(policyDocument);

		Mockito.verify(auditLogRepository).create(any());
	}

	@Test
	void shouldDetectPolicyDocumentUpdateWithRevision() {
		PolicyId policyId = new PolicyId(UUID.randomUUID());
		PolicyDocument policyDocument = PolicyDocument.builder()
			.id(policyId)
			.siteId("siteId")
			.build();
		Site site = Site.builder()
			.build();

		when(repository.update(policyDocument, true)).thenReturn(policyId);
		when(repository.findById(policyId)).thenReturn(Optional.of(policyDocument));
		when(siteRepository.findById("siteId")).thenReturn(Optional.of(site));

		service.updateWithRevision(policyDocument);

		Mockito.verify(auditLogRepository).create(any());
	}

	@Test
	void shouldDetectPolicyDocumentCreation() {
		PolicyId policyId = new PolicyId(UUID.randomUUID());
		PolicyDocument policyDocument = PolicyDocument.builder()
			.id(policyId)
			.build();
		when(repository.create(policyDocument)).thenReturn(policyId);
		when(repository.findById(policyId)).thenReturn(Optional.of(policyDocument));

		service.create(policyDocument);

		Mockito.verify(auditLogRepository).create(any());
	}

	@Test
	void shouldDetectPolicyDocumentAcceptation() {
		FenixUserId userId = new FenixUserId("userId");
		PolicyId policyId = new PolicyId(UUID.randomUUID());
		PolicyDocument policyDocument = PolicyDocument.builder()
			.id(policyId)
			.revision(1)
			.siteId("siteId")
			.build();
		PolicyAcceptance policyAcceptance = PolicyAcceptance.builder()
			.policyDocumentId(policyId)
			.policyDocumentRevision(1)
			.build();
		FURMSUser user = FURMSUser.builder()
			.email("email")
			.fenixUserId(userId).build();

		when(usersDAO.findById(userId)).thenReturn(Optional.of(user));
		when(repository.findById(policyId)).thenReturn(Optional.of(policyDocument));

		service.addUserPolicyAcceptance("siteId", userId, policyAcceptance);

		Mockito.verify(auditLogRepository).create(any());
	}
}
