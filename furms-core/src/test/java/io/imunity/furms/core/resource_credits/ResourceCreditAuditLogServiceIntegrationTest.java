/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.core.resource_credits;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.imunity.furms.api.authz.AuthzService;
import io.imunity.furms.api.resource_types.ResourceTypeService;
import io.imunity.furms.core.community_allocation.CommunityAllocationServiceHelper;
import io.imunity.furms.domain.resource_credits.ResourceCredit;
import io.imunity.furms.spi.audit_log.AuditLogRepository;
import io.imunity.furms.spi.community_allocation.CommunityAllocationRepository;
import io.imunity.furms.spi.resource_credits.ResourceCreditRepository;
import io.imunity.furms.spi.resource_type.ResourceTypeRepository;
import io.imunity.furms.spi.resource_usage.ResourceUsageRepository;
import io.imunity.furms.spi.sites.SiteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.ApplicationEventPublisher;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SpringBootTest
@SpringBootApplication(scanBasePackages = "io.imunity.furms.core.audit_log", scanBasePackageClasses = ResourceCreditAuditLogService.class)
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
		String id = "id";
		ResourceCredit mock = mock(ResourceCredit.class);
		when(resourceCreditRepository.findById(id)).thenReturn(Optional.of(mock));

		//when
		service.delete(id, "");

		Mockito.verify(auditLogRepository).create(any());
	}

	@Test
	void shouldDetectResourceCreditUpdate() {
		//given
		ResourceCredit request = ResourceCredit.builder()
			.id("id")
			.siteId("id")
			.resourceTypeId("id")
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

		Mockito.verify(auditLogRepository).create(any());
	}

	@Test
	void shouldDetectResourceCreditCreation() {
		//given
		ResourceCredit request = ResourceCredit.builder()
			.id("id")
			.siteId("id")
			.resourceTypeId("id")
			.name("name")
			.amount(new BigDecimal(1))
			.utcStartTime(LocalDateTime.now())
			.utcEndTime(LocalDateTime.now())
			.build();

		when(siteRepository.exists(request.siteId)).thenReturn(true);
		when(resourceTypeRepository.exists(request.resourceTypeId)).thenReturn(true);
		when(resourceCreditRepository.isNamePresent(request.name, request.siteId)).thenReturn(false);
		when(resourceCreditRepository.create(request)).thenReturn("id");
		when(resourceCreditRepository.findById("id")).thenReturn(Optional.of(request));

		//when
		service.create(request);

		Mockito.verify(auditLogRepository).create(any());
	}
}
