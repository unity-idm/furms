/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.core.project_installation;

import io.imunity.furms.core.project_allocation_installation.ProjectAllocationInstallationService;
import io.imunity.furms.domain.project_installation.ProjectInstallationJob;
import io.imunity.furms.domain.project_installation.ProjectInstallationResult;
import io.imunity.furms.domain.project_installation.ProjectUpdateJob;
import io.imunity.furms.domain.project_installation.ProjectUpdateResult;
import io.imunity.furms.domain.project_installation.ProjectUpdateStatus;
import io.imunity.furms.domain.site_agent.CorrelationId;
import io.imunity.furms.spi.project_installation.ProjectOperationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;
import java.util.Optional;

import static io.imunity.furms.domain.project_installation.ProjectInstallationStatus.ACKNOWLEDGED;
import static io.imunity.furms.domain.project_installation.ProjectInstallationStatus.INSTALLED;
import static io.imunity.furms.domain.project_installation.ProjectInstallationStatus.PENDING;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProjectInstallationStatusUpdaterTest {
	@Mock
	private ProjectOperationRepository repository;
	@Mock
	private ProjectAllocationInstallationService projectAllocationInstallationService;

	private ProjectInstallationStatusUpdaterImpl service;
	private InOrder orderVerifier;

	@BeforeEach
	void init() {
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
		when(repository.findInstallationJobByCorrelationId(id)).thenReturn(Optional.of(projectInstallationJob));
		ProjectInstallationResult result = new ProjectInstallationResult(Map.of(), ACKNOWLEDGED, null);
		service.update(id, result);

		//then
		orderVerifier.verify(repository).update("id", result);
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
		when(repository.findInstallationJobByCorrelationId(id)).thenReturn(Optional.of(projectInstallationJob));
		ProjectInstallationResult result = new ProjectInstallationResult(Map.of("gid", "gid"), INSTALLED, null);
		service.update(id, result);

		//then
		orderVerifier.verify(repository).update("id", result);
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
		when(repository.findUpdateJobByCorrelationId(id)).thenReturn(Optional.of(projectInstallationJob));
		ProjectUpdateResult result = new ProjectUpdateResult(ProjectUpdateStatus.UPDATED, null);
		service.update(id, result);

		//then
		orderVerifier.verify(repository).update("id", result);
	}
}