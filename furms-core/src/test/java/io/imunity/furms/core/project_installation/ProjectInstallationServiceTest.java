/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.core.project_installation;

import io.imunity.furms.domain.project_installation.ProjectInstallation;
import io.imunity.furms.domain.project_installation.ProjectInstallationJob;
import io.imunity.furms.domain.project_installation.ProjectUpdateJob;
import io.imunity.furms.domain.projects.Project;
import io.imunity.furms.domain.sites.SiteExternalId;
import io.imunity.furms.domain.sites.SiteId;
import io.imunity.furms.domain.users.FURMSUser;
import io.imunity.furms.domain.users.PersistentId;
import io.imunity.furms.site.api.site_agent.SiteAgentProjectOperationService;
import io.imunity.furms.spi.project_installation.ProjectOperationRepository;
import io.imunity.furms.spi.sites.SiteRepository;
import io.imunity.furms.spi.users.UsersDAO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.when;

class ProjectInstallationServiceTest {
	@Mock
	private ProjectOperationRepository repository;
	@Mock
	private SiteAgentProjectOperationService siteAgentProjectOperationService;
	@Mock
	private UsersDAO usersDAO;
	@Mock
	private SiteRepository siteRepository;

	private ProjectInstallationServiceImpl service;
	private InOrder orderVerifier;

	@BeforeEach
	void init() {
		MockitoAnnotations.initMocks(this);
		service = new ProjectInstallationServiceImpl(repository, siteAgentProjectOperationService, usersDAO, siteRepository);
		orderVerifier = inOrder(repository, siteAgentProjectOperationService);
	}

	@Test
	void shouldCreateProjectInstallation() {
		//given
		ProjectInstallation projectInstallation = ProjectInstallation.builder().build();
		//when
		when(repository.findProjectInstallation(eq("projectAllocationId"), any()))
			.thenReturn(projectInstallation);
		service.create("projectId", projectInstallation);

		//then
		orderVerifier.verify(repository).create(any(ProjectInstallationJob.class));
		orderVerifier.verify(siteAgentProjectOperationService).installProject(any(), any());
	}

	@Test
	void shouldCreateProjectUpdate() {
		//given
		PersistentId userId = new PersistentId("userId");
		Project project = Project.builder()
			.id("id")
			.leaderId(userId)
			.build();
		FURMSUser user = FURMSUser.builder()
			.id(userId)
			.email("email")
			.build();

		//when
		when(usersDAO.findById(userId)).thenReturn(Optional.of(user));
		when(siteRepository.findByProjectId("id")).thenReturn(Set.of(new SiteId("siteId", new SiteExternalId("id"))));
		service.update(project);

		//then
		orderVerifier.verify(repository).create(any(ProjectUpdateJob.class));
		orderVerifier.verify(siteAgentProjectOperationService).updateProject(any(), any(), any(), any());
	}
}