/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.core.project_allocation_installation;

import io.imunity.furms.domain.project_allocation.ProjectAllocationResolved;
import io.imunity.furms.domain.project_allocation_installation.ProjectAllocationInstallation;
import io.imunity.furms.domain.project_allocation_installation.ProjectAllocationInstallationStatus;
import io.imunity.furms.domain.project_allocation_installation.ProjectDeallocation;
import io.imunity.furms.domain.sites.Site;
import io.imunity.furms.site.api.site_agent.SiteAgentProjectAllocationInstallationService;
import io.imunity.furms.spi.project_allocation_installation.ProjectAllocationInstallationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.ArgumentMatchers.any;
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
		orderVerifier = inOrder(repository, siteAgentProjectAllocationInstallationService);
	}

	@Test
	void shouldCreateProjectAllocationInstallation() {
		//given
		ProjectAllocationInstallation projectAllocationInstallation = ProjectAllocationInstallation.builder()
				.status(ProjectAllocationInstallationStatus.PENDING)
				.build();

		//when
		service.createAllocation("communityId", projectAllocationInstallation, null);

		//then
		orderVerifier.verify(repository).create(any(ProjectAllocationInstallation.class));
		orderVerifier.verify(siteAgentProjectAllocationInstallationService).allocateProject(any(), any());
	}

	@Test
	void shouldCreateProjectDeallocation() {
		//given
		ProjectAllocationResolved projectAllocationInstallation = ProjectAllocationResolved.builder()
			.site(Site.builder().build())
			.build();

		//when
		service.createDeallocation("communityId", projectAllocationInstallation);

		//then
		orderVerifier.verify(repository).create(any(ProjectDeallocation.class));
		orderVerifier.verify(siteAgentProjectAllocationInstallationService).deallocateProject(any(), any());
	}
}