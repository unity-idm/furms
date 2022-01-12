/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.core.community_allocation;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.imunity.furms.api.authz.AuthzService;
import io.imunity.furms.api.project_allocation.ProjectAllocationService;
import io.imunity.furms.domain.community_allocation.CommunityAllocation;
import io.imunity.furms.spi.audit_log.AuditLogRepository;
import io.imunity.furms.spi.community_allocation.CommunityAllocationRepository;
import io.imunity.furms.spi.resource_usage.ResourceUsageRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.ApplicationEventPublisher;

import java.math.BigDecimal;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@SpringBootTest
@SpringBootApplication(scanBasePackages = "io.imunity.furms.core.audit_log", scanBasePackageClasses = CommunityAllocationAuditLogService.class)
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

		Mockito.verify(auditLogRepository).create(any());
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

		Mockito.verify(auditLogRepository).create(any());
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

		Mockito.verify(auditLogRepository).create(any());
	}
}
