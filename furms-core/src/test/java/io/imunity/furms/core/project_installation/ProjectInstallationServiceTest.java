/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.core.project_installation;

import io.imunity.furms.domain.project_installation.ProjectInstallationJob;
import io.imunity.furms.domain.site_agent.CorrelationId;
import io.imunity.furms.site.api.site_agent.SiteAgentProjectOperationService;
import io.imunity.furms.spi.project_installation.ProjectOperationRepository;
import io.imunity.furms.spi.users.UsersDAO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static io.imunity.furms.domain.project_installation.ProjectInstallationStatus.PENDING;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.inOrder;

class ProjectInstallationServiceTest {
	@Mock
	private ProjectOperationRepository repository;
	@Mock
	private SiteAgentProjectOperationService siteAgentProjectOperationService;
	@Mock
	private UsersDAO usersDAO;

	private ProjectInstallationServiceImpl service;
	private InOrder orderVerifier;

	@BeforeEach
	void init() {
		MockitoAnnotations.initMocks(this);
		service = new ProjectInstallationServiceImpl(repository, siteAgentProjectOperationService, usersDAO);
		orderVerifier = inOrder(repository);
	}

	@Test
	void shouldCreateProjectInstallation() {
		//given
		CorrelationId id = new CorrelationId("id");
		ProjectInstallationJob projectInstallationJob = ProjectInstallationJob.builder()
				.correlationId(id)
				.status(PENDING)
				.build();

		//when
		service.create("communityId", projectInstallationJob, null);

		//then
		orderVerifier.verify(repository).create(eq(projectInstallationJob));
	}

	@Test
	void shouldDeleteProjectInstallation() {
		service.delete("id", "id");

		orderVerifier.verify(repository).delete(eq("id"));
	}
}