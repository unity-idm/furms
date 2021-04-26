/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.core.project_allocation_installation;

import io.imunity.furms.domain.project_allocation_installation.ProjectAllocationInstallation;
import io.imunity.furms.domain.project_allocation_installation.ProjectAllocationInstallationStatus;
import io.imunity.furms.domain.site_agent.CorrelationId;
import io.imunity.furms.site.api.site_agent.SiteAgentProjectAllocationInstallationService;
import io.imunity.furms.spi.project_allocation_installation.ProjectAllocationInstallationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.inOrder;

class ProjectAllocationInstallationServiceTest {
	@Mock
	private ProjectAllocationInstallationRepository repository;
	@Mock
	private SiteAgentProjectAllocationInstallationService siteAgentProjectAllocationInstallationService;


	private ProjectAllocationInstallationServiceImpl service;
	private InOrder orderVerifier;

	@BeforeEach
	void init() {
		MockitoAnnotations.initMocks(this);
		service = new ProjectAllocationInstallationServiceImpl(repository, siteAgentProjectAllocationInstallationService);
		orderVerifier = inOrder(repository);
	}

	@Test
	void shouldCreateProjectAllocationInstallation() {
		//given
		CorrelationId id = new CorrelationId("id");
		ProjectAllocationInstallation projectAllocationInstallation = ProjectAllocationInstallation.builder()
				.correlationId(id)
				.status(ProjectAllocationInstallationStatus.SENT)
				.build();

		//when
		service.create("communityId", projectAllocationInstallation, null);

		//then
		orderVerifier.verify(repository).create(eq(projectAllocationInstallation));
	}

	@Test
	void shouldDeleteProjectInstallation() {
		service.delete("id", "id");

		orderVerifier.verify(repository).delete(eq("id"));
	}
}