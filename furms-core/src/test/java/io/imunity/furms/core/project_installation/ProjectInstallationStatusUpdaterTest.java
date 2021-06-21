/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.core.project_installation;

import io.imunity.furms.core.project_allocation_installation.ProjectAllocationInstallationService;
import io.imunity.furms.domain.project_installation.ProjectInstallationJob;
import io.imunity.furms.domain.project_installation.ProjectInstallationResult;
import io.imunity.furms.domain.project_installation.ProjectInstallationStatus;
import io.imunity.furms.domain.project_installation.ProjectUpdateJob;
import io.imunity.furms.domain.project_installation.ProjectUpdateResult;
import io.imunity.furms.domain.project_installation.ProjectUpdateStatus;
import io.imunity.furms.domain.site_agent.CorrelationId;
import io.imunity.furms.spi.project_installation.ProjectOperationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Map;

import static io.imunity.furms.domain.project_installation.ProjectInstallationStatus.PENDING;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.when;

class ProjectInstallationStatusUpdaterTest {
	@Mock
	private ProjectOperationRepository repository;
	@Mock
	private ProjectAllocationInstallationService projectAllocationInstallationService;

	private ProjectInstallationStatusUpdaterImpl service;
	private InOrder orderVerifier;

	@BeforeEach
	void init() {
		MockitoAnnotations.initMocks(this);
		service = new ProjectInstallationStatusUpdaterImpl(repository, projectAllocationInstallationService);
		orderVerifier = inOrder(repository, projectAllocationInstallationService);
	}

	@Test
	void shouldUpdateProjectInstallation() {
		//given
		CorrelationId id = new CorrelationId("id");
		ProjectInstallationJob projectInstallationJob = ProjectInstallationJob.builder()
				.id("id")
				.correlationId(id)
				.status(PENDING)
				.build();

		//when
		when(repository.findInstallationJobByCorrelationId(id)).thenReturn(projectInstallationJob);
		service.update(id, new ProjectInstallationResult(Map.of(), ProjectInstallationStatus.ACKNOWLEDGED, null));

		//then
		orderVerifier.verify(repository).update("id", ProjectInstallationStatus.ACKNOWLEDGED, null);
	}

	@Test
	void shouldUpdateProjectInstallationAndStartAllocationsInstallation() {
		//given
		CorrelationId id = new CorrelationId("id");
		ProjectInstallationJob projectInstallationJob = ProjectInstallationJob.builder()
			.id("id")
			.projectId("projectId")
			.siteId("siteId")
			.correlationId(id)
			.status(PENDING)
			.build();

		//when
		when(repository.findInstallationJobByCorrelationId(id)).thenReturn(projectInstallationJob);
		service.update(id, new ProjectInstallationResult(Map.of("gid", "gid"), ProjectInstallationStatus.INSTALLED, null));

		//then
		orderVerifier.verify(repository).update("id", ProjectInstallationStatus.INSTALLED, "gid");
		orderVerifier.verify(projectAllocationInstallationService).startWaitingAllocations("projectId", "siteId");
	}

	@Test
	void shouldUpdateProjectUpdate() {
		//given
		CorrelationId id = new CorrelationId("id");
		ProjectUpdateJob projectInstallationJob = ProjectUpdateJob.builder()
			.id("id")
			.correlationId(id)
			.status(ProjectUpdateStatus.PENDING)
			.build();

		//when
		when(repository.findUpdateJobByCorrelationId(id)).thenReturn(projectInstallationJob);
		service.update(id, new ProjectUpdateResult(ProjectUpdateStatus.UPDATED, null));

		//then
		orderVerifier.verify(repository).update("id", ProjectUpdateStatus.UPDATED);
	}
}