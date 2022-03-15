/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.core.project_allocation_installation;

import io.imunity.furms.domain.project_allocation.ProjectAllocationResolved;
import io.imunity.furms.domain.project_allocation_installation.ProjectAllocationInstallation;
import io.imunity.furms.domain.project_allocation_installation.ProjectAllocationInstallationStatus;
import io.imunity.furms.domain.project_allocation_installation.ProjectDeallocation;
import io.imunity.furms.domain.site_agent.CorrelationId;
import io.imunity.furms.domain.sites.Site;
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
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.Optional;
import java.util.Set;

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
	private ApplicationEventPublisher publisher;
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
		when(projectAllocationRepository.findByIdWithRelatedObjects("projectAllocationId"))
			.thenReturn(Optional.of(ProjectAllocationResolved.builder()
				.site(Site.builder().build())
				.build()));

		//when
		service.createAllocation("projectAllocationId");

		//then
		orderVerifier.verify(repository).create(any(ProjectAllocationInstallation.class));
	}

	@Test
	void shouldStartProjectAllocationInstallation() {
		//given
		CorrelationId correlationId = CorrelationId.randomID();

		when(repository.findAll("projectId", "siteId"))
			.thenReturn(Set.of(ProjectAllocationInstallation.builder()
				.correlationId(correlationId)
				.siteId("siteId")
				.projectAllocationId("projectAllocationId")
				.build()));
		when(projectAllocationRepository.findByIdWithRelatedObjects("projectAllocationId"))
			.thenReturn(Optional.of(ProjectAllocationResolved.builder()
				.site(Site.builder().build())
				.build()));

		//when
		service.startWaitingAllocations("projectId", "siteId");

		//then
		orderVerifier.verify(repository).update(correlationId.id, ProjectAllocationInstallationStatus.PENDING, Optional.empty());
		orderVerifier.verify(siteAgentProjectAllocationInstallationService).allocateProject(eq(correlationId), any());
	}

	@Test
	void shouldCreateProjectDeallocation() {
		//given
		ProjectAllocationResolved projectAllocationInstallation = ProjectAllocationResolved.builder()
			.id("id")
			.site(Site.builder()
				.id("id")
				.build())
			.build();

		//when
		when(repository.findByProjectAllocationId("id")).thenReturn(ProjectAllocationInstallation.builder()
			.status(ProjectAllocationInstallationStatus.ACKNOWLEDGED)
			.build());
		service.createDeallocation(projectAllocationInstallation);

		//then
		orderVerifier.verify(repository).create(any(ProjectDeallocation.class));
		orderVerifier.verify(siteAgentProjectAllocationInstallationService).deallocateProject(any(), any());
	}

	@Test
	void shouldDeleteProjectAllocationIfFailed() {
		//given
		ProjectAllocationResolved projectAllocationInstallation = ProjectAllocationResolved.builder()
			.id("id")
			.site(Site.builder()
				.id("id")
				.build())
			.build();

		//when
		when(repository.findByProjectAllocationId("id")).thenReturn(ProjectAllocationInstallation.builder()
			.id("id")
			.projectAllocationId("id")
			.status(ProjectAllocationInstallationStatus.PROJECT_INSTALLATION_FAILED)
			.build());
		service.createDeallocation(projectAllocationInstallation);

		//then
		orderVerifier.verify(projectAllocationRepository).deleteById("id");
	}
}