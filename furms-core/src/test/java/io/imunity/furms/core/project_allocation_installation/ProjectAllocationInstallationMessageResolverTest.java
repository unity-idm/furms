/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.core.project_allocation_installation;

import io.imunity.furms.domain.project_allocation_installation.ProjectAllocationInstallationStatus;
import io.imunity.furms.domain.project_allocation_installation.ProjectDeallocationStatus;
import io.imunity.furms.domain.site_agent.CorrelationId;
import io.imunity.furms.spi.project_allocation_installation.ProjectAllocationInstallationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.Mockito.inOrder;

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
	void shouldUpdateProjectAllocationInstallation() {
		//given
		CorrelationId id = new CorrelationId("id");

		//when
		service.updateStatus(id, ProjectAllocationInstallationStatus.PROVISIONING_PROJECT);

		//then
		orderVerifier.verify(repository).update(id.id, ProjectAllocationInstallationStatus.PROVISIONING_PROJECT);
	}

	@Test
	void shouldUpdateProjectDeallocation() {
		//given
		CorrelationId id = new CorrelationId("id");

		//when
		service.updateStatus(id, ProjectDeallocationStatus.PENDING);

		//then
		orderVerifier.verify(repository).update(id.id, ProjectDeallocationStatus.PENDING);
	}
}