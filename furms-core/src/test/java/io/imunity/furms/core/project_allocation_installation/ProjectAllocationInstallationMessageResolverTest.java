/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.core.project_allocation_installation;

import io.imunity.furms.domain.project_allocation_installation.ProjectAllocationInstallation;
import io.imunity.furms.domain.project_allocation_installation.ProjectAllocationInstallationStatus;
import io.imunity.furms.domain.site_agent.CorrelationId;
import io.imunity.furms.spi.project_allocation_installation.ProjectAllocationInstallationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;

import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.when;

class ProjectAllocationInstallationMessageResolverTest {
	@Mock
	private ProjectAllocationInstallationRepository repository;

	private ProjectAllocationInstallationMessageResolverImpl service;
	private InOrder orderVerifier;

	@BeforeEach
	void init() {
		MockitoAnnotations.initMocks(this);
		service = new ProjectAllocationInstallationMessageResolverImpl(repository);
		orderVerifier = inOrder(repository);
	}

	@Test
	void shouldUpdateProjectInstallation() {
		//given
		CorrelationId id = new CorrelationId("id");
		ProjectAllocationInstallation projectAllocationInstallation = ProjectAllocationInstallation.builder()
				.id("id")
				.correlationId(id)
				.status(ProjectAllocationInstallationStatus.SENT)
				.build();

		//when
		when(repository.findByCorrelationId(id)).thenReturn(Optional.of(projectAllocationInstallation));
		service.updateStatus(id, ProjectAllocationInstallationStatus.SENT);

		//then
		orderVerifier.verify(repository).update("id", ProjectAllocationInstallationStatus.SENT);
	}
}