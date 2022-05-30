/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.core.project_allocation;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.imunity.furms.api.authz.AuthzService;
import io.imunity.furms.core.audit_log.AuditLogPackageTestExposer;
import io.imunity.furms.core.project_allocation_installation.ProjectAllocationInstallationService;
import io.imunity.furms.core.project_installation.ProjectInstallationService;
import io.imunity.furms.domain.audit_log.Action;
import io.imunity.furms.domain.audit_log.AuditLog;
import io.imunity.furms.domain.audit_log.Operation;
import io.imunity.furms.domain.communities.CommunityId;
import io.imunity.furms.domain.community_allocation.CommunityAllocationId;
import io.imunity.furms.domain.project_allocation.ProjectAllocation;
import io.imunity.furms.domain.project_allocation.ProjectAllocationId;
import io.imunity.furms.domain.project_allocation.ProjectAllocationResolved;
import io.imunity.furms.domain.project_installation.ProjectInstallation;
import io.imunity.furms.domain.projects.ProjectId;
import io.imunity.furms.domain.sites.SiteId;
import io.imunity.furms.site.api.site_agent.SiteAgentProjectAllocationInstallationService;
import io.imunity.furms.site.api.site_agent.SiteAgentProjectOperationService;
import io.imunity.furms.spi.audit_log.AuditLogRepository;
import io.imunity.furms.spi.project_allocation.ProjectAllocationRepository;
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
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@SpringBootTest
@SpringBootApplication(scanBasePackageClasses = {ProjectAllocationAuditLogService.class, AuditLogPackageTestExposer.class})
class ProjectAllocationAuditLogServiceIntegrationTest {
	@MockBean
	private ProjectAllocationServiceValidator validator;
	@MockBean
	private ProjectAllocationRepository projectAllocationRepository;
	@MockBean
	private ProjectInstallationService projectInstallationService;
	@MockBean
	private SiteAgentProjectOperationService siteAgentProjectOperationService;
	@MockBean
	private SiteAgentProjectAllocationInstallationService siteAgentProjectAllocationInstallationService;
	@MockBean
	private ProjectAllocationInstallationService projectAllocationInstallationServiMockBean;

	@MockBean
	private AuthzService authzService;
	@MockBean
	private ObjectMapper objectMapper;
	@Autowired
	private ApplicationEventPublisher publisher;
	@MockBean
	private AuditLogRepository auditLogRepository;

	private ProjectAllocationServiceImpl service;

	@BeforeEach
	void init() {
		service = new ProjectAllocationServiceImpl(projectAllocationRepository, projectInstallationService, validator,
			projectAllocationInstallationServiMockBean, publisher);
	}

	@Test
	void shouldDetectProjectAllocationDeletion() {
		//given
		CommunityId communityId = new CommunityId(UUID.randomUUID());
		ProjectAllocationId id = new ProjectAllocationId(UUID.randomUUID());
		ProjectAllocationResolved projectAllocationResolved = ProjectAllocationResolved.builder()
			.id(id)
			.amount(BigDecimal.TEN)
			.consumed(BigDecimal.ZERO)
			.build();
		ProjectAllocation projectAllocation = ProjectAllocation.builder()
			.id(id)
			.build();
		when(projectAllocationRepository.findByIdWithRelatedObjects(id)).thenReturn(Optional.of(projectAllocationResolved));
		when(projectAllocationRepository.findById(id)).thenReturn(Optional.of(projectAllocation));

		//when
		service.delete(communityId, id);

		ArgumentCaptor<AuditLog> argument = ArgumentCaptor.forClass(AuditLog.class);
		Mockito.verify(auditLogRepository).create(argument.capture());
		assertEquals(Operation.PROJECT_ALLOCATION, argument.getValue().operationCategory);
		assertEquals(Action.DELETE, argument.getValue().action);
	}

	@Test
	void shouldDetectProjectAllocationUpdate() {
		//given
		SiteId siteId = new SiteId(UUID.randomUUID());
		CommunityId communityId = new CommunityId(UUID.randomUUID());
		ProjectId projectId = new ProjectId(UUID.randomUUID());
		ProjectAllocationId id = new ProjectAllocationId(UUID.randomUUID());

		ProjectAllocation request = ProjectAllocation.builder()
			.id(id)
			.projectId(new ProjectId(UUID.randomUUID()))
			.communityAllocationId(new CommunityAllocationId(UUID.randomUUID()))
			.name("name")
			.amount(new BigDecimal(1))
			.build();

		when(projectInstallationService.findProjectInstallationOfProjectAllocation( id)).thenReturn(
			ProjectInstallation.builder()
				.siteId(new SiteId(UUID.randomUUID()))
				.build()
		);
		when(projectInstallationService.isProjectInstalled(siteId, projectId)).thenReturn(true);
		when(projectAllocationRepository.findById(id)).thenReturn(Optional.of(request));

		//when
		service.update(communityId, request);

		ArgumentCaptor<AuditLog> argument = ArgumentCaptor.forClass(AuditLog.class);
		Mockito.verify(auditLogRepository).create(argument.capture());
		assertEquals(Operation.PROJECT_ALLOCATION, argument.getValue().operationCategory);
		assertEquals(Action.UPDATE, argument.getValue().action);
	}

	@Test
	void shouldDetectProjectAllocationCreation() {
		//given
		CommunityId communityId = new CommunityId(UUID.randomUUID());
		ProjectAllocationId id = new ProjectAllocationId(UUID.randomUUID());

		ProjectAllocation request = ProjectAllocation.builder()
			.id(id)
			.projectId(new ProjectId(UUID.randomUUID()))
			.communityAllocationId(new CommunityAllocationId(UUID.randomUUID()))
			.name("name")
			.amount(new BigDecimal(1))
			.build();

		//when
		when(projectInstallationService.findProjectInstallationOfProjectAllocation( id)).thenReturn(
			ProjectInstallation.builder()
				.siteId(new SiteId(UUID.randomUUID()))
				.build()
		);
		when(projectAllocationRepository.create(request)).thenReturn(id);
		when(projectAllocationRepository.findById(id)).thenReturn(Optional.of(request));

		service.create(communityId, request);

		ArgumentCaptor<AuditLog> argument = ArgumentCaptor.forClass(AuditLog.class);
		Mockito.verify(auditLogRepository).create(argument.capture());
		assertEquals(Operation.PROJECT_ALLOCATION, argument.getValue().operationCategory);
		assertEquals(Action.CREATE, argument.getValue().action);
	}
}
