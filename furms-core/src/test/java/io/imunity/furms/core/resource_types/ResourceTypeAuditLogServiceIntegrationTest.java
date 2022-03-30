/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.core.resource_types;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.imunity.furms.api.authz.AuthzService;
import io.imunity.furms.core.audit_log.AuditLogPackageTestExposer;
import io.imunity.furms.domain.audit_log.Action;
import io.imunity.furms.domain.audit_log.AuditLog;
import io.imunity.furms.domain.audit_log.Operation;
import io.imunity.furms.domain.resource_types.ResourceMeasureType;
import io.imunity.furms.domain.resource_types.ResourceMeasureUnit;
import io.imunity.furms.domain.resource_types.ResourceType;
import io.imunity.furms.spi.audit_log.AuditLogRepository;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@SpringBootTest
@SpringBootApplication(scanBasePackageClasses = {ResourceTypeAuditLogService.class, AuditLogPackageTestExposer.class})
class ResourceTypeAuditLogServiceIntegrationTest {
	@MockBean
	private ResourceTypeRepository resourceTypeRepository;
	@MockBean
	private SiteRepository siteRepository;
	@MockBean
	private InfraServiceRepository infraServiceRepository;
	@MockBean
	private ResourceCreditRepository resourceCreditRepository;
	@MockBean
	private ResourceTypeServiceValidator validator;

	@MockBean
	private AuthzService authzService;
	@MockBean
	private ObjectMapper objectMapper;
	@Autowired
	private ApplicationEventPublisher publisher;
	@MockBean
	private AuditLogRepository auditLogRepository;

	private ResourceTypeServiceImpl service;

	@BeforeEach
	void init() {
		service = new ResourceTypeServiceImpl(resourceTypeRepository, validator, publisher);
	}

	@Test
	void shouldDetectResourceTypeDeletion() {
		//given
		String id = "id";
		when(resourceTypeRepository.exists(id)).thenReturn(true);
		ResourceType resourceType = ResourceType.builder().build();
		when(resourceTypeRepository.findById("id")).thenReturn(Optional.of(resourceType));

		//when
		service.delete(id, "");

		ArgumentCaptor<AuditLog> argument = ArgumentCaptor.forClass(AuditLog.class);
		Mockito.verify(auditLogRepository).create(argument.capture());
		assertEquals(Operation.RESOURCE_TYPES_MANAGEMENT, argument.getValue().operationCategory);
		assertEquals(Action.DELETE, argument.getValue().action);
	}

	@Test
	void shouldDetectResourceTypeUpdate() {
		//given
		ResourceType request = ResourceType.builder()
			.id("id")
			.siteId("id")
			.serviceId("id")
			.name("name")
			.type(ResourceMeasureType.DATA)
			.unit(ResourceMeasureUnit.GB)
			.build();

		when(siteRepository.exists(request.siteId)).thenReturn(true);
		when(infraServiceRepository.exists(request.serviceId)).thenReturn(true);
		when(resourceTypeRepository.exists(request.id)).thenReturn(true);
		when(resourceTypeRepository.findById(request.id)).thenReturn(Optional.of(request));

		//when
		service.update(request);

		ArgumentCaptor<AuditLog> argument = ArgumentCaptor.forClass(AuditLog.class);
		Mockito.verify(auditLogRepository).create(argument.capture());
		assertEquals(Operation.RESOURCE_TYPES_MANAGEMENT, argument.getValue().operationCategory);
		assertEquals(Action.UPDATE, argument.getValue().action);
	}

	@Test
	void shouldDetectResourceTypeCreation() {
		//given
		ResourceType request = ResourceType.builder()
			.id("id")
			.siteId("id")
			.serviceId("id")
			.name("name")
			.type(ResourceMeasureType.DATA)
			.unit(ResourceMeasureUnit.GB)
			.build();

		when(siteRepository.exists(request.siteId)).thenReturn(true);
		when(infraServiceRepository.exists(request.serviceId)).thenReturn(true);
		when(resourceTypeRepository.findById("id")).thenReturn(Optional.of(request));
		when(resourceTypeRepository.create(request)).thenReturn("id");

		//when
		service.create(request);

		ArgumentCaptor<AuditLog> argument = ArgumentCaptor.forClass(AuditLog.class);
		Mockito.verify(auditLogRepository).create(argument.capture());
		assertEquals(Operation.RESOURCE_TYPES_MANAGEMENT, argument.getValue().operationCategory);
		assertEquals(Action.CREATE, argument.getValue().action);
	}
}
