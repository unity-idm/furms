/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.core.community_allocation;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.imunity.furms.api.authz.AuthzService;
import io.imunity.furms.api.project_allocation.ProjectAllocationService;
import io.imunity.furms.core.audit_log.AuditLogServiceImplTest;
import io.imunity.furms.domain.audit_log.Action;
import io.imunity.furms.domain.audit_log.AuditLog;
import io.imunity.furms.domain.audit_log.Operation;
import io.imunity.furms.domain.community_allocation.CommunityAllocation;
import io.imunity.furms.spi.audit_log.AuditLogRepository;
import io.imunity.furms.spi.community_allocation.CommunityAllocationRepository;
import io.imunity.furms.spi.resource_usage.ResourceUsageRepository;
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
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@SpringBootTest
@SpringBootApplication(scanBasePackageClasses = {CommunityAllocationAuditLogService.class, AuditLogServiceImplTest.class})
class CommunityAllocationAuditLogServiceIntegrationTest {
	@MockBean
	private CommunityAllocationServiceValidator validator;
	@MockBean
	private CommunityAllocationRepository communityAllocationRepository;
	@MockBean
	private ProjectAllocationService projectAllocationService;
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

	private CommunityAllocationServiceImpl service;

	@BeforeEach
	void init() {
		service = new CommunityAllocationServiceImpl(communityAllocationRepository, validator,
			publisher, projectAllocationService, resourceUsageRepository);
	}

	@Test
	void shouldDetectCommunityAllocationDeletion() {
		//given
		String id = "id";
		CommunityAllocation request = CommunityAllocation.builder()
			.id("id")
			.communityId("id")
			.resourceCreditId("id")
			.name("name")
			.amount(new BigDecimal(1))
			.build();
		when(communityAllocationRepository.findById("id")).thenReturn(Optional.of(request));

		//when
		service.delete(id);

		ArgumentCaptor<AuditLog> argument = ArgumentCaptor.forClass(AuditLog.class);
		Mockito.verify(auditLogRepository).create(argument.capture());
		assertEquals(Operation.COMMUNITY_ALLOCATION, argument.getValue().operationCategory);
		assertEquals(Action.DELETE, argument.getValue().action);
	}

	@Test
	void shouldDetectCommunityAllocationUpdate() {
		//given
		CommunityAllocation request = CommunityAllocation.builder()
			.id("id")
			.communityId("id")
			.resourceCreditId("id")
			.name("name")
			.amount(new BigDecimal(1))
			.build();
		when(communityAllocationRepository.findById("id")).thenReturn(Optional.of(request));

		//when
		service.update(request);

		ArgumentCaptor<AuditLog> argument = ArgumentCaptor.forClass(AuditLog.class);
		Mockito.verify(auditLogRepository).create(argument.capture());
		assertEquals(Operation.COMMUNITY_ALLOCATION, argument.getValue().operationCategory);
		assertEquals(Action.UPDATE, argument.getValue().action);
	}

	@Test
	void shouldDetectCommunityAllocationCreation() {
		//given
		CommunityAllocation request = CommunityAllocation.builder()
			.id("id")
			.communityId("id")
			.resourceCreditId("id")
			.name("name")
			.amount(new BigDecimal(1))
			.build();
		when(communityAllocationRepository.findById("id")).thenReturn(Optional.of(request));
		when(communityAllocationRepository.create(request)).thenReturn("id");

		//when
		service.create(request);

		ArgumentCaptor<AuditLog> argument = ArgumentCaptor.forClass(AuditLog.class);
		Mockito.verify(auditLogRepository).create(argument.capture());
		assertEquals(Operation.COMMUNITY_ALLOCATION, argument.getValue().operationCategory);
		assertEquals(Action.CREATE, argument.getValue().action);
	}
}
