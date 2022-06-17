/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.core.project_allocation_installation;

import io.imunity.furms.domain.project_allocation.ProjectAllocationId;
import io.imunity.furms.domain.project_allocation.ProjectAllocationResolved;
import io.imunity.furms.domain.project_allocation_installation.ProjectAllocationInstallation;
import io.imunity.furms.domain.project_allocation_installation.ProjectAllocationInstallationId;
import io.imunity.furms.domain.project_allocation_installation.ProjectAllocationInstallationStatus;
import io.imunity.furms.domain.project_allocation_installation.ProjectDeallocation;
import io.imunity.furms.domain.projects.ProjectId;
import io.imunity.furms.domain.site_agent.CorrelationId;
import io.imunity.furms.domain.sites.Site;
import io.imunity.furms.domain.sites.SiteId;
import io.imunity.furms.site.api.site_agent.SiteAgentProjectAllocationInstallationService;
import io.imunity.furms.spi.project_allocation.ProjectAllocationRepository;
import io.imunity.furms.spi.project_allocation_installation.ProjectAllocationInstallationRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.when;

@SpringBootTest(classes = SpringBootLauncher.class)
@ExtendWith(MockitoExtension.class)
class ProjectAllocationInstallationServiceTest {
	@Autowired
	private ProjectAllocationInstallationRepository repository;
	@Autowired
	private ProjectAllocationRepository projectAllocationRepository;
	@Autowired
	private SiteAgentProjectAllocationInstallationService siteAgentProjectAllocationInstallationService;
	@Autowired
	private ProjectAllocationInstallationService service;

	private InOrder orderVerifier;

	@BeforeEach
	void setUp() {
		TransactionSynchronizationManager.initSynchronization();
	}

	@AfterEach
	void clear() {
		TransactionSynchronizationManager.clear();
	}

	@BeforeEach
	void init() {
		orderVerifier = inOrder(repository, siteAgentProjectAllocationInstallationService, projectAllocationRepository);
	}

	@Test
	void shouldCreateProjectAllocationInstallation() {
		//given
		ProjectAllocationId projectAllocationId = new ProjectAllocationId(UUID.randomUUID());
		when(projectAllocationRepository.findByIdWithRelatedObjects(projectAllocationId))
			.thenReturn(Optional.of(ProjectAllocationResolved.builder()
				.site(Site.builder().id(new SiteId(UUID.randomUUID())).build())
				.build()));

		//when
		service.createAllocation(projectAllocationId);

		//then
		orderVerifier.verify(repository).create(any(ProjectAllocationInstallation.class));
	}

	@Test
	void shouldUpdateAndStartAllocation() {
		ProjectAllocationId projectAllocationId = new ProjectAllocationId(UUID.randomUUID());
		ProjectAllocationResolved projectAllocationResolved = ProjectAllocationResolved.builder().build();

		when(projectAllocationRepository.findByIdWithRelatedObjects(projectAllocationId))
			.thenReturn(Optional.of(projectAllocationResolved));

		service.updateAndStartAllocation(projectAllocationId);

		orderVerifier.verify(repository).update(eq(projectAllocationId),
			eq(ProjectAllocationInstallationStatus.UPDATING), any(CorrelationId.class)
		);
		orderVerifier.verify(siteAgentProjectAllocationInstallationService).allocateProject(
			any(), eq(projectAllocationResolved)
		);
	}

	@Test
	void shouldCreateAndStartAllocation() {
		ProjectAllocationId projectAllocationId = new ProjectAllocationId(UUID.randomUUID());
		ProjectAllocationResolved projectAllocationResolved = ProjectAllocationResolved.builder()
			.site(Site.builder().id(new SiteId(UUID.randomUUID())).build())
			.build();
		when(projectAllocationRepository.findByIdWithRelatedObjects(projectAllocationId))
			.thenReturn(Optional.of(projectAllocationResolved));

		service.createAndStartAllocation(projectAllocationId);

		orderVerifier.verify(repository).create(any(ProjectAllocationInstallation.class));
		orderVerifier.verify(siteAgentProjectAllocationInstallationService).allocateProject(
			any(), eq(projectAllocationResolved)
		);
	}

	@Test
	void shouldStartProjectAllocationInstallation() {
		//given
		CorrelationId correlationId = CorrelationId.randomID();
		ProjectAllocationId projectAllocationId = new ProjectAllocationId(UUID.randomUUID());
		ProjectId projectId = new ProjectId(UUID.randomUUID());
		SiteId siteId = new SiteId(UUID.randomUUID());

		when(repository.findAll(projectId, siteId))
			.thenReturn(Set.of(ProjectAllocationInstallation.builder()
				.correlationId(correlationId)
				.siteId(siteId)
				.projectAllocationId(projectAllocationId)
				.build()));
		when(projectAllocationRepository.findByIdWithRelatedObjects(projectAllocationId))
			.thenReturn(Optional.of(ProjectAllocationResolved.builder()
				.site(Site.builder().id(new SiteId(UUID.randomUUID())).build())
				.build()));

		//when
		service.startWaitingAllocations(projectId, siteId);

		//then
		orderVerifier.verify(repository).update(correlationId, ProjectAllocationInstallationStatus.PENDING, Optional.empty());
		orderVerifier.verify(siteAgentProjectAllocationInstallationService).allocateProject(eq(correlationId), any());
	}

	@Test
	void shouldCreateProjectDeallocation() {
		//given
		ProjectAllocationId projectAllocationId = new ProjectAllocationId(UUID.randomUUID());
		SiteId siteId = new SiteId(UUID.randomUUID());
		ProjectAllocationResolved projectAllocationInstallation = ProjectAllocationResolved.builder()
			.id(projectAllocationId)
			.site(Site.builder()
				.id(siteId)
				.build())
			.build();

		//when
		when(repository.findByProjectAllocationId(projectAllocationId)).thenReturn(ProjectAllocationInstallation.builder()
			.status(ProjectAllocationInstallationStatus.ACKNOWLEDGED)
			.siteId(siteId)
			.build()
		);
		service.createDeallocation(projectAllocationInstallation);

		//then
		orderVerifier.verify(repository).create(any(ProjectDeallocation.class));
		orderVerifier.verify(siteAgentProjectAllocationInstallationService).deallocateProject(any(), any());
	}

	@Test
	void shouldDeleteProjectAllocationIfFailed() {
		//given
		ProjectAllocationInstallationId projectAllocationInstallationId = new ProjectAllocationInstallationId(UUID.randomUUID());
		ProjectAllocationId projectAllocationId = new ProjectAllocationId(UUID.randomUUID());
		ProjectAllocationResolved projectAllocationInstallation = ProjectAllocationResolved.builder()
			.id(projectAllocationId)
			.site(Site.builder()
				.id(new SiteId(UUID.randomUUID()))
				.build())
			.build();

		//when
		when(repository.findByProjectAllocationId(projectAllocationId)).thenReturn(ProjectAllocationInstallation.builder()
			.id(projectAllocationInstallationId)
			.projectAllocationId(projectAllocationId)
			.status(ProjectAllocationInstallationStatus.PROJECT_INSTALLATION_FAILED)
			.build());
		service.createDeallocation(projectAllocationInstallation);

		//then
		orderVerifier.verify(projectAllocationRepository).deleteById(projectAllocationId);
	}
}