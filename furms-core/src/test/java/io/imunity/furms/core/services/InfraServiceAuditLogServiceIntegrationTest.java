/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.core.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.imunity.furms.api.authz.AuthzService;
import io.imunity.furms.core.audit_log.AuditLogPackageTestExposer;
import io.imunity.furms.core.policy_documents.PolicyNotificationService;
import io.imunity.furms.domain.audit_log.Action;
import io.imunity.furms.domain.audit_log.AuditLog;
import io.imunity.furms.domain.audit_log.Operation;
import io.imunity.furms.domain.policy_documents.PolicyDocument;
import io.imunity.furms.domain.policy_documents.PolicyId;
import io.imunity.furms.domain.services.InfraService;
import io.imunity.furms.domain.services.InfraServiceId;
import io.imunity.furms.domain.sites.SiteExternalId;
import io.imunity.furms.domain.sites.SiteId;
import io.imunity.furms.site.api.site_agent.SiteAgentPolicyDocumentService;
import io.imunity.furms.spi.audit_log.AuditLogRepository;
import io.imunity.furms.spi.policy_docuemnts.PolicyDocumentRepository;
import io.imunity.furms.spi.resource_credits.ResourceCreditRepository;
import io.imunity.furms.spi.resource_type.ResourceTypeRepository;
import io.imunity.furms.spi.services.InfraServiceRepository;
import io.imunity.furms.spi.sites.SiteRepository;
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
import static org.mockito.Mockito.when;

@SpringBootTest
@SpringBootApplication(scanBasePackageClasses = {InfraServiceAuditLogService.class, AuditLogPackageTestExposer.class})
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
		SiteId siteId = new SiteId(UUID.randomUUID());
		InfraServiceId id = new InfraServiceId(UUID.randomUUID());
		when(infraServiceRepository.exists(id)).thenReturn(true);
		InfraService infraService = InfraService.builder()
			.id(id)
			.build();
		when(infraServiceRepository.findById(id)).thenReturn(Optional.of(infraService));

		//when
		service.delete(id, siteId);

		ArgumentCaptor<AuditLog> argument = ArgumentCaptor.forClass(AuditLog.class);
		Mockito.verify(auditLogRepository).create(argument.capture());
		assertEquals(Operation.SERVICES_MANAGEMENT, argument.getValue().operationCategory);
		assertEquals(Action.DELETE, argument.getValue().action);
	}

	@Test
	void shouldDetectInfraServiceUpdate() {
		//given
		SiteId siteId = new SiteId(UUID.randomUUID());
		InfraServiceId id = new InfraServiceId(UUID.randomUUID());
		PolicyId policyId = new PolicyId(UUID.randomUUID());
		PolicyDocument policyDocument = PolicyDocument.builder()
			.id(policyId)
			.name("policyName")
			.revision(1)
			.build();
		SiteExternalId siteExternalId = new SiteExternalId("id");
		InfraService oldService = InfraService.builder()
			.id(id)
			.siteId(siteId)
			.name("userFacingName")
			.policyId(policyId)
			.build();

		InfraService newService = InfraService.builder()
			.id(id)
			.siteId(siteId)
			.name("userFacingName")
			.policyId(null)
			.build();

		when(siteRepository.exists(oldService.siteId)).thenReturn(true);
		when(infraServiceRepository.exists(oldService.id)).thenReturn(true);
		when(infraServiceRepository.findById(oldService.id)).thenReturn(Optional.of(oldService));
		when(policyDocumentRepository.findById(policyId)).thenReturn(Optional.of(policyDocument));
		when(siteRepository.findByIdExternalId(oldService.siteId)).thenReturn(siteExternalId);

		//when
		service.update(newService);

		ArgumentCaptor<AuditLog> argument = ArgumentCaptor.forClass(AuditLog.class);
		Mockito.verify(auditLogRepository).create(argument.capture());
		assertEquals(Operation.SERVICES_MANAGEMENT, argument.getValue().operationCategory);
		assertEquals(Action.UPDATE, argument.getValue().action);
	}

	@Test
	void shouldDetectInfraServiceCreation() {
		//given
		SiteId siteId = new SiteId(UUID.randomUUID());
		InfraServiceId id = new InfraServiceId(UUID.randomUUID());
		InfraService request = InfraService.builder()
			.id(id)
			.siteId(siteId)
			.name("userFacingName")
			.build();

		when(siteRepository.exists(siteId)).thenReturn(true);
		when(infraServiceRepository.findById(id)).thenReturn(Optional.of(request));
		when(infraServiceRepository.create(request)).thenReturn(id);

		//when
		service.create(request);

		ArgumentCaptor<AuditLog> argument = ArgumentCaptor.forClass(AuditLog.class);
		Mockito.verify(auditLogRepository).create(argument.capture());
		assertEquals(Operation.SERVICES_MANAGEMENT, argument.getValue().operationCategory);
		assertEquals(Action.CREATE, argument.getValue().action);
	}
}
