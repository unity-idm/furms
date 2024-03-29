/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.core.project_allocation_installation;

import io.imunity.furms.domain.project_allocation.ProjectAllocationId;
import io.imunity.furms.domain.project_allocation_installation.ProjectAllocationChunk;
import io.imunity.furms.domain.project_allocation_installation.ProjectAllocationInstallation;
import io.imunity.furms.domain.project_allocation_installation.ProjectAllocationInstallationStatus;
import io.imunity.furms.domain.project_allocation_installation.ProjectDeallocation;
import io.imunity.furms.domain.project_allocation_installation.ProjectDeallocationStatus;
import io.imunity.furms.domain.site_agent.CorrelationId;
import io.imunity.furms.spi.project_allocation.ProjectAllocationRepository;
import io.imunity.furms.spi.project_allocation_installation.ProjectAllocationInstallationRepository;
import io.imunity.furms.spi.resource_access.ResourceAccessRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProjectAllocationInstallationStatusUpdaterTest {
	@Mock
	private ProjectAllocationInstallationRepository repository;
	@Mock
	private ProjectAllocationRepository projectAllocationRepository;
	@Mock
	private ResourceAccessRepository resourceAccessRepository;
	@Mock
	private ApplicationEventPublisher publisher;


	private ProjectAllocationInstallationStatusUpdaterImpl service;
	private InOrder orderVerifier;

	@BeforeEach
	void init() {
		service = new ProjectAllocationInstallationStatusUpdaterImpl(repository, projectAllocationRepository, resourceAccessRepository, publisher);
		orderVerifier = inOrder(repository);
	}

	@Test
	void shouldReturnTrueIfAllocationWaitingForConfirmation() {
		//given
		ProjectAllocationId projectAllocationId = new ProjectAllocationId(UUID.randomUUID());

		//when
		when(repository.findByProjectAllocationId(projectAllocationId)).thenReturn(ProjectAllocationInstallation.builder()
			.projectAllocationId(projectAllocationId)
			.status(ProjectAllocationInstallationStatus.INSTALLATION_ACKNOWLEDGED)
			.build());
		//then
		assertTrue(service.isWaitingForInstallationConfirmation(projectAllocationId));
	}

	@Test
	void shouldReturnFalseIfChunkIsNotWaitingForConfirmation() {
		//given
		ProjectAllocationId projectAllocationId = new ProjectAllocationId(UUID.randomUUID());

		//when
		when(repository.findByProjectAllocationId(projectAllocationId)).thenReturn(ProjectAllocationInstallation.builder()
			.projectAllocationId(projectAllocationId)
			.status(ProjectAllocationInstallationStatus.INSTALLED)
			.build());

		//then
		assertFalse(service.isWaitingForInstallationConfirmation(projectAllocationId));
	}

	@Test
	void shouldUpdateProjectAllocationInstallationStatusByProjectAllocationId() {
		//given
		ProjectAllocationId projectAllocationId = new ProjectAllocationId(UUID.randomUUID());

		//when
		when(repository.findByProjectAllocationId(projectAllocationId)).thenReturn(ProjectAllocationInstallation.builder()
			.projectAllocationId(projectAllocationId)
			.status(ProjectAllocationInstallationStatus.INSTALLING)
			.build());
		service.updateStatus(projectAllocationId, ProjectAllocationInstallationStatus.INSTALLED, Optional.empty());

		//then
		orderVerifier.verify(repository).update(projectAllocationId,
			ProjectAllocationInstallationStatus.INSTALLED, Optional.empty());
	}

	@Test
	void shouldUpdateProjectAllocationInstallationStatusAndCreateUserAddition() {
		//given
		CorrelationId id = new CorrelationId("id");

		//when
		when(repository.findByCorrelationId(id)).thenReturn(Optional.of(ProjectAllocationInstallation.builder()
			.projectAllocationId(UUID.randomUUID().toString())
			.status(ProjectAllocationInstallationStatus.INSTALLING)
			.build()));
		service.updateStatus(id, ProjectAllocationInstallationStatus.INSTALLATION_ACKNOWLEDGED, Optional.empty());

		//then
		orderVerifier.verify(repository).update(id, ProjectAllocationInstallationStatus.INSTALLATION_ACKNOWLEDGED, Optional.empty());
	}

	@Test
	void shouldUpdateProjectAllocationInstallationStatusAndDoNotCreateUserAddition() {
		//given
		CorrelationId id = new CorrelationId("id");

		//when
		when(repository.findByCorrelationId(id)).thenReturn(Optional.of(ProjectAllocationInstallation.builder()
			.projectAllocationId(UUID.randomUUID().toString())
			.status(ProjectAllocationInstallationStatus.INSTALLING)
			.build()));
		service.updateStatus(id, ProjectAllocationInstallationStatus.INSTALLATION_ACKNOWLEDGED, Optional.empty());

		//then
		orderVerifier.verify(repository).update(id, ProjectAllocationInstallationStatus.INSTALLATION_ACKNOWLEDGED, Optional.empty());
	}

	@Test
	void shouldCreateProjectAllocationChunk() {
		//given
		ProjectAllocationId projectAllocationId = new ProjectAllocationId(UUID.randomUUID());
		ProjectAllocationChunk chunk = ProjectAllocationChunk.builder()
			.projectAllocationId(projectAllocationId)
			.build();

		//when
		when(repository.findByProjectAllocationId(projectAllocationId)).thenReturn(ProjectAllocationInstallation.builder()
			.status(ProjectAllocationInstallationStatus.INSTALLED)
			.build());
		service.createChunk(chunk);

		//then
		orderVerifier.verify(repository).create(chunk);
	}

	@Test
	void shouldNotCreateProjectAllocationChunkWhenAllocationInstallationIsNotTerminal() {
		//given
		ProjectAllocationId projectAllocationId = new ProjectAllocationId(UUID.randomUUID());
		ProjectAllocationChunk chunk = ProjectAllocationChunk.builder()
			.projectAllocationId(projectAllocationId)
			.build();

		//when
		when(repository.findByProjectAllocationId(projectAllocationId)).thenReturn(ProjectAllocationInstallation.builder()
			.status(ProjectAllocationInstallationStatus.INSTALLING)
			.build());

		assertThrows(IllegalArgumentException.class, () -> service.createChunk(chunk));
	}

	@Test
	void shouldUpdateProjectDeallocation() {
		//given
		CorrelationId id = new CorrelationId("id");

		//when
		when(repository.findDeallocationByCorrelationId(id)).thenReturn(Optional.of(ProjectDeallocation.builder()
			.status(ProjectDeallocationStatus.PENDING)
			.build()));
		service.updateStatus(id, ProjectDeallocationStatus.PENDING, Optional.empty());

		//then
		orderVerifier.verify(repository).update(id, ProjectDeallocationStatus.PENDING, Optional.empty());
	}
}