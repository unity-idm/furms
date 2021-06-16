/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.core.project_allocation_installation;

import io.imunity.furms.domain.project_allocation_installation.*;
import io.imunity.furms.domain.site_agent.CorrelationId;
import io.imunity.furms.spi.project_allocation.ProjectAllocationRepository;
import io.imunity.furms.spi.project_allocation_installation.ProjectAllocationInstallationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.when;

class ProjectAllocationInstallationStatusUpdaterTest {
	@Mock
	private ProjectAllocationInstallationRepository repository;
	@Mock
	private ProjectAllocationRepository projectAllocationRepository;


	private ProjectAllocationInstallationStatusUpdaterImpl service;
	private InOrder orderVerifier;

	@BeforeEach
	void init() {
		MockitoAnnotations.initMocks(this);
		service = new ProjectAllocationInstallationStatusUpdaterImpl(repository, projectAllocationRepository);
		orderVerifier = inOrder(repository);
	}

	@Test
	void shouldUpdateProjectAllocationInstallation() {
		//given
		CorrelationId id = new CorrelationId("id");

		//when
		when(repository.findByCorrelationId(id)).thenReturn(Optional.of(ProjectAllocationInstallation.builder()
			.status(ProjectAllocationInstallationStatus.PENDING)
			.build()));
		service.updateStatus(id, ProjectAllocationInstallationStatus.ACKNOWLEDGED, Optional.empty());

		//then
		orderVerifier.verify(repository).update(id.id, ProjectAllocationInstallationStatus.ACKNOWLEDGED, Optional.empty());
	}

	@Test
	void shouldCreateProjectAllocationChunk() {
		//given
		ProjectAllocationChunk chunk = ProjectAllocationChunk.builder()
			.projectAllocationId("id")
			.build();

		//when
		when(repository.findByProjectAllocationId("id")).thenReturn(ProjectAllocationInstallation.builder()
			.status(ProjectAllocationInstallationStatus.ACKNOWLEDGED)
			.build());
		service.createChunk(chunk);

		//then
		orderVerifier.verify(repository).create(chunk);
	}

	@Test
	void shouldNotCreateProjectAllocationChunkWhenAllocationInstallationIsNotTerminal() {
		//given
		ProjectAllocationChunk chunk = ProjectAllocationChunk.builder()
			.projectAllocationId("id")
			.build();

		//when
		when(repository.findByProjectAllocationId("id")).thenReturn(ProjectAllocationInstallation.builder()
			.status(ProjectAllocationInstallationStatus.PENDING)
			.build());

		assertThrows(IllegalArgumentException.class, () -> service.createChunk(chunk));
	}

	@Test
	void shouldUpdateProjectDeallocation() {
		//given
		CorrelationId id = new CorrelationId("id");

		//when
		when(repository.findDeallocationByCorrelationId(id.id)).thenReturn(ProjectDeallocation.builder()
			.status(ProjectDeallocationStatus.PENDING)
			.build());
		service.updateStatus(id, ProjectDeallocationStatus.PENDING, null);

		//then
		orderVerifier.verify(repository).update(id.id, ProjectDeallocationStatus.PENDING, null);
	}
}