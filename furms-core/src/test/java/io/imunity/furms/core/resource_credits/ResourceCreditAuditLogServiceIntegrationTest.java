/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.core.resource_credits;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.imunity.furms.api.authz.AuthzService;
import io.imunity.furms.api.resource_types.ResourceTypeService;
import io.imunity.furms.core.audit_log.AuditLogPackageTestExposer;
import io.imunity.furms.core.community_allocation.CommunityAllocationServiceHelper;
import io.imunity.furms.domain.audit_log.Action;
import io.imunity.furms.domain.audit_log.AuditLog;
import io.imunity.furms.domain.audit_log.Operation;
import io.imunity.furms.domain.resource_credits.ResourceCredit;
import io.imunity.furms.domain.resource_credits.ResourceCreditId;
import io.imunity.furms.domain.resource_types.ResourceTypeId;
import io.imunity.furms.domain.sites.SiteId;
import io.imunity.furms.spi.audit_log.AuditLogRepository;
import io.imunity.furms.spi.community_allocation.CommunityAllocationRepository;
import io.imunity.furms.spi.resource_credits.ResourceCreditRepository;
import io.imunity.furms.spi.resource_type.ResourceTypeRepository;
import io.imunity.furms.spi.resource_usage.ResourceUsageRepository;
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

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@SpringBootTest
@SpringBootApplication(scanBasePackageClasses = {ResourceCreditAuditLogService.class, AuditLogPackageTestExposer.class})
class ResourceCreditAuditLogServiceIntegrationTest {
	@MockBean
	private ResourceCreditServiceValidator validator;
	@MockBean
	private ResourceCreditRepository resourceCreditRepository;
	@MockBean
	private SiteRepository siteRepository;
	@MockBean
	private ResourceTypeRepository resourceTypeRepository;
	@MockBean
	private CommunityAllocationRepository communityAllocationRepository;
	@MockBean
	private CommunityAllocationServiceHelper communityAllocationServiceHelper;
	@MockBean
	private ResourceTypeService resourceTypeService;
	@MockBean
	private ResourceUsageRepository resourceUsageRepository;

	@MockBean
	private AuthzService authzService;
	@MockBean
	private ObjectMapper objectMapper;
	@Autowired
	private ApplicationEventPublisher publisher;
	@MockBean
	private AuditLogRepository auditLogRepository;

	private ResourceCreditServiceImpl service;

	@BeforeEach
	void init() {
		service = new ResourceCreditServiceImpl(resourceCreditRepository, validator, publisher,
			communityAllocationServiceHelper, authzService, resourceTypeService, resourceUsageRepository);
	}

	@Test
	void shouldDetectResourceCreditDeletion() {
		//given
		SiteId siteId = new SiteId(UUID.randomUUID());
		ResourceCreditId id = new ResourceCreditId(UUID.randomUUID());
		ResourceCredit mock = ResourceCredit.builder()
			.id(id)
			.build();
		when(resourceCreditRepository.findById(id)).thenReturn(Optional.of(mock));

		//when
		service.delete(id, siteId);

		ArgumentCaptor<AuditLog> argument = ArgumentCaptor.forClass(AuditLog.class);
		Mockito.verify(auditLogRepository).create(argument.capture());
		assertEquals(Operation.RESOURCE_CREDIT, argument.getValue().operationCategory);
		assertEquals(Action.DELETE, argument.getValue().action);
	}

	@Test
	void shouldDetectResourceCreditUpdate() {
		//given
		ResourceCredit request = ResourceCredit.builder()
			.id(UUID.randomUUID().toString())
			.siteId(UUID.randomUUID().toString())
			.resourceTypeId(UUID.randomUUID().toString())
			.name("name")
			.amount(new BigDecimal(1))
			.utcStartTime(LocalDateTime.now())
			.utcEndTime(LocalDateTime.now())
			.build();

		when(siteRepository.exists(request.siteId)).thenReturn(true);
		when(communityAllocationRepository.getAvailableAmount(request.id)).thenReturn(BigDecimal.ZERO);
		when(resourceTypeRepository.exists(request.resourceTypeId)).thenReturn(true);
		when(resourceCreditRepository.findById(request.id)).thenReturn(Optional.of(request));

		//when
		service.update(request);

		ArgumentCaptor<AuditLog> argument = ArgumentCaptor.forClass(AuditLog.class);
		Mockito.verify(auditLogRepository).create(argument.capture());
		assertEquals(Operation.RESOURCE_CREDIT, argument.getValue().operationCategory);
		assertEquals(Action.UPDATE, argument.getValue().action);
	}

	@Test
	void shouldDetectResourceCreditCreation() {
		//given
		SiteId siteId = new SiteId(UUID.randomUUID());
		ResourceTypeId resourceTypeId = new ResourceTypeId(UUID.randomUUID());
		ResourceCreditId resourceCreditId = new ResourceCreditId(UUID.randomUUID());
		ResourceCredit request = ResourceCredit.builder()
			.id(resourceCreditId)
			.siteId(siteId)
			.resourceTypeId(resourceTypeId)
			.name("name")
			.amount(new BigDecimal(1))
			.utcStartTime(LocalDateTime.now())
			.utcEndTime(LocalDateTime.now())
			.build();

		when(siteRepository.exists(request.siteId)).thenReturn(true);
		when(resourceTypeRepository.exists(request.resourceTypeId)).thenReturn(true);
		when(resourceCreditRepository.isNamePresent(request.name, request.siteId)).thenReturn(false);
		when(resourceCreditRepository.create(request)).thenReturn(resourceCreditId);
		when(resourceCreditRepository.findById(resourceCreditId)).thenReturn(Optional.of(request));

		//when
		service.create(request);

		ArgumentCaptor<AuditLog> argument = ArgumentCaptor.forClass(AuditLog.class);
		Mockito.verify(auditLogRepository).create(argument.capture());
		assertEquals(Operation.RESOURCE_CREDIT, argument.getValue().operationCategory);
		assertEquals(Action.CREATE, argument.getValue().action);
	}
}
