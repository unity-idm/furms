/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.core.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.imunity.furms.api.authz.AuthzService;
import io.imunity.furms.core.policy_documents.PolicyNotificationService;
import io.imunity.furms.domain.policy_documents.PolicyDocument;
import io.imunity.furms.domain.policy_documents.PolicyId;
import io.imunity.furms.domain.services.InfraService;
import io.imunity.furms.domain.sites.SiteExternalId;
import io.imunity.furms.site.api.site_agent.SiteAgentPolicyDocumentService;
import io.imunity.furms.spi.audit_log.AuditLogRepository;
import io.imunity.furms.spi.policy_docuemnts.PolicyDocumentRepository;
import io.imunity.furms.spi.resource_credits.ResourceCreditRepository;
import io.imunity.furms.spi.resource_type.ResourceTypeRepository;
import io.imunity.furms.spi.services.InfraServiceRepository;
import io.imunity.furms.spi.sites.SiteRepository;
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
@SpringBootApplication(scanBasePackages = "io.imunity.furms.core.audit_log", scanBasePackageClasses = InfraServiceAuditLogService.class)
class InfraServiceAuditLogServiceIntegrationTest {
	@MockBean
	private InfraServiceRepository infraServiceRepository;
	@MockBean
	private SiteRepository siteRepository;
	@MockBean
	private PolicyDocumentRepository policyDocumentRepository;
	@MockBean
	private ResourceTypeRepository resourceTypeRepository;
	@MockBean
	private ResourceCreditRepository resourceCreditRepository;
	@MockBean
	private SiteAgentPolicyDocumentService siteAgentPolicyDocumentService;
	@MockBean
	private PolicyNotificationService policyNotificationService;
	@MockBean
	private InfraServiceServiceValidator validator;

	@MockBean
	private AuthzService authzService;
	@MockBean
	private ObjectMapper objectMapper;
	@Autowired
	private ApplicationEventPublisher publisher;
	@MockBean
	private AuditLogRepository auditLogRepository;

	private InfraServiceServiceImpl service;

	@BeforeEach
	void init() {
		service = new InfraServiceServiceImpl(infraServiceRepository, validator, siteAgentPolicyDocumentService, siteRepository, policyDocumentRepository, publisher, policyNotificationService);
	}

	@Test
	void shouldDetectInfraServiceDeletion() {
		//given
		String id = "id";
		when(infraServiceRepository.exists(id)).thenReturn(true);
		InfraService infraService = InfraService.builder().build();
		when(infraServiceRepository.findById(id)).thenReturn(Optional.of(infraService));

		//when
		service.delete(id, "");

		Mockito.verify(auditLogRepository).create(any());
	}

	@Test
	void shouldDetectInfraServiceUpdate() {
		//given
		PolicyId policyId = new PolicyId(UUID.randomUUID());
		PolicyDocument policyDocument = PolicyDocument.builder()
			.id(policyId)
			.name("policyName")
			.revision(1)
			.build();
		SiteExternalId siteExternalId = new SiteExternalId("id");
		InfraService oldService = InfraService.builder()
			.id("id")
			.siteId("id")
			.name("userFacingName")
			.policyId(policyId)
			.build();

		InfraService newService = InfraService.builder()
			.id("id")
			.siteId("id")
			.name("userFacingName")
			.policyId(null)
			.build();

		when(siteRepository.exists(oldService.id)).thenReturn(true);
		when(infraServiceRepository.exists(oldService.id)).thenReturn(true);
		when(infraServiceRepository.findById(oldService.id)).thenReturn(Optional.of(oldService));
		when(policyDocumentRepository.findById(policyId)).thenReturn(Optional.of(policyDocument));
		when(siteRepository.findByIdExternalId("id")).thenReturn(siteExternalId);

		//when
		service.update(newService);

		Mockito.verify(auditLogRepository).create(any());
	}

	@Test
	void shouldDetectInfraServiceCreation() {
		//given
		InfraService request = InfraService.builder()
			.id("id")
			.siteId("id")
			.name("userFacingName")
			.build();

		when(siteRepository.exists(request.id)).thenReturn(true);
		when(infraServiceRepository.findById("id")).thenReturn(Optional.of(request));
		when(infraServiceRepository.create(request)).thenReturn("id");

		//when
		service.create(request);

		Mockito.verify(auditLogRepository).create(any());
	}
}
