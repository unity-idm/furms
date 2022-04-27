/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.core.project_installation;

import io.imunity.furms.domain.communities.Community;
import io.imunity.furms.domain.communities.CommunityId;
import io.imunity.furms.domain.project_installation.ProjectInstallation;
import io.imunity.furms.domain.project_installation.ProjectInstallationJob;
import io.imunity.furms.domain.project_installation.ProjectInstallationStatus;
import io.imunity.furms.domain.project_installation.ProjectUpdateJob;
import io.imunity.furms.domain.project_installation.ProjectUpdateStatus;
import io.imunity.furms.domain.projects.Project;
import io.imunity.furms.domain.projects.ProjectId;
import io.imunity.furms.domain.sites.SiteExternalId;
import io.imunity.furms.domain.sites.SiteId;
import io.imunity.furms.domain.users.FURMSUser;
import io.imunity.furms.domain.users.PersistentId;
import io.imunity.furms.site.api.site_agent.SiteAgentProjectOperationService;
import io.imunity.furms.spi.communites.CommunityRepository;
import io.imunity.furms.spi.project_installation.ProjectOperationRepository;
import io.imunity.furms.spi.sites.SiteRepository;
import io.imunity.furms.spi.users.UsersDAO;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.when;

@SpringBootTest(classes = SpringBootLauncher.class)
@ExtendWith(MockitoExtension.class)
class ProjectInstallationServiceTest {
	@Autowired
	private ProjectOperationRepository repository;
	@Autowired
	private SiteAgentProjectOperationService siteAgentProjectOperationService;
	@Autowired
	private UsersDAO usersDAO;
	@Autowired
	private SiteRepository siteRepository;
	@Autowired
	private CommunityRepository communityRepository;
	@Autowired
	private ApplicationEventPublisher publisher;
	@Autowired
	private ProjectInstallationServiceImpl service;

	private InOrder orderVerifier;

	@BeforeEach
	void setUp() {
		TransactionSynchronizationManager.initSynchronization();
	}

	@AfterEach
	void clear() {
		TransactionSynchronizationManager.clear();
	}

	@BeforeEach
	void init() {
		orderVerifier = inOrder(repository, siteAgentProjectOperationService);
	}

	@Test
	void shouldCreateProjectInstallation() {
		//given
		ProjectId projectId = new ProjectId(UUID.randomUUID());
		ProjectInstallation projectInstallation = ProjectInstallation.builder()
			.id(projectId)
			.siteId(new SiteId(UUID.randomUUID()))
			.build();

		service.createOrUpdate(projectId, projectInstallation);
		for (TransactionSynchronization transactionSynchronization : TransactionSynchronizationManager
			.getSynchronizations()) {
			transactionSynchronization.afterCommit();
		}

		//then
		orderVerifier.verify(repository).createOrUpdate(any(ProjectInstallationJob.class));
		orderVerifier.verify(siteAgentProjectOperationService).installProject(any(), any());
	}

	@Test
	void shouldCreateProjectUpdate() {
		//given
		ProjectId projectId = new ProjectId(UUID.randomUUID());
		PersistentId userId = new PersistentId("userId");
		SiteId siteId = new SiteId(UUID.randomUUID().toString(), new SiteExternalId("id"));
		Project project = Project.builder()
			.id(projectId)
			.leaderId(userId)
			.build();
		FURMSUser user = FURMSUser.builder()
			.id(userId)
			.email("email")
			.build();

		//when
		when(usersDAO.findById(userId)).thenReturn(Optional.of(user));
		when(siteRepository.findByProjectId(projectId)).thenReturn(Set.of(siteId));
		when(repository.findProjectInstallation(project.getId())).thenReturn(Set.of(
			ProjectInstallationJob.builder()
				.siteId(siteId)
				.projectId(projectId)
				.status(ProjectInstallationStatus.INSTALLED)
				.build()
		));
		when(repository.findProjectUpdateStatues(project.getId())).thenReturn(Set.of());
		when(siteRepository.findByProjectId(projectId)).thenReturn(Set.of(siteId));

		service.update(project);

		//then
		orderVerifier.verify(repository).createOrUpdate(any(ProjectUpdateJob.class));

		orderVerifier.verify(siteAgentProjectOperationService).updateProject(any(), any(), any(), any());
	}

	@Test
	void shouldCreateProjectInstallationInsteadOfUpdate() {
		//given
		CommunityId communityId = new CommunityId(UUID.randomUUID());
		ProjectId projectId = new ProjectId(UUID.randomUUID());
		PersistentId userId = new PersistentId("userId");
		SiteId siteId = new SiteId(UUID.randomUUID().toString(), new SiteExternalId("id"));
		Project project = Project.builder()
			.id(projectId)
			.communityId(communityId)
			.leaderId(userId)
			.build();
		FURMSUser user = FURMSUser.builder()
			.id(userId)
			.email("email")
			.build();

		//when
		when(usersDAO.findById(userId)).thenReturn(Optional.of(user));
		when(siteRepository.findByProjectId(projectId)).thenReturn(Set.of(siteId));
		when(repository.findProjectInstallation(project.getId())).thenReturn(Set.of(
			ProjectInstallationJob.builder()
				.siteId(siteId)
				.projectId(projectId)
				.status(ProjectInstallationStatus.FAILED)
				.build()
		));
		when(siteRepository.findByProjectId(projectId)).thenReturn(Set.of(siteId));
		when(communityRepository.findById(communityId)).thenReturn(Optional.of(Community.builder()
			.id(communityId)
			.name("name")
			.build()));

		service.update(project);

		//then
		orderVerifier.verify(repository).createOrUpdate(any(ProjectInstallationJob.class));
		orderVerifier.verify(siteAgentProjectOperationService).installProject(any(), any());
	}

	@Test
	void shouldNotCreateProjectUpdateWhenProjectIsUpdating() {
		//given
		PersistentId userId = new PersistentId("userId");
		Project project = Project.builder()
			.id(new ProjectId(UUID.randomUUID()))
			.leaderId(userId)
			.build();

		//when
		when(repository.findProjectInstallation(project.getId())).thenReturn(Set.of(
			ProjectInstallationJob.builder()
				.siteId(new SiteId(UUID.randomUUID()))
				.status(ProjectInstallationStatus.INSTALLED)
				.build()
		));
		when(repository.findProjectUpdateStatues(project.getId())).thenReturn(Set.of(ProjectUpdateStatus.ACKNOWLEDGED));

		assertThrows(IllegalStateException.class, () -> service.update(project));
	}
}