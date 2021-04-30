/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.core.project_installation;

import io.imunity.furms.domain.project_installation.*;
import io.imunity.furms.domain.site_agent.CorrelationId;
import io.imunity.furms.spi.project_installation.ProjectOperationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static io.imunity.furms.domain.project_installation.ProjectInstallationStatus.PENDING;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.when;

class ProjectInstallationMessageResolverTest {
	@Mock
	private ProjectOperationRepository repository;

	private ProjectInstallationMessageResolverImpl service;
	private InOrder orderVerifier;

	@BeforeEach
	void init() {
		MockitoAnnotations.initMocks(this);
		service = new ProjectInstallationMessageResolverImpl(repository);
		orderVerifier = inOrder(repository);
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
		service.update(id, ProjectInstallationStatus.ACKNOWLEDGED);

		//then
		orderVerifier.verify(repository).update("id", ProjectInstallationStatus.ACKNOWLEDGED);
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
		service.update(id, ProjectUpdateStatus.UPDATED);

		//then
		orderVerifier.verify(repository).update("id", ProjectUpdateStatus.UPDATED);
	}

	@Test
	void shouldUpdateProjectRemoval() {
		//given
		CorrelationId id = new CorrelationId("id");
		ProjectRemovalJob projectInstallationJob = ProjectRemovalJob.builder()
			.id("id")
			.correlationId(id)
			.status(ProjectRemovalStatus.PENDING)
			.build();

		//when
		when(repository.findRemovalJobByCorrelationId(id)).thenReturn(projectInstallationJob);
		service.update(id, ProjectRemovalStatus.FAILED);

		//then
		orderVerifier.verify(repository).update("id", ProjectRemovalStatus.FAILED);
	}
}