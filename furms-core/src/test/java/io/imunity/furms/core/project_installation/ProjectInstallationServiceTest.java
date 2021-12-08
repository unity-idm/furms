/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.core.project_installation;

import io.imunity.furms.domain.communities.Community;
import io.imunity.furms.domain.project_installation.ProjectInstallation;
import io.imunity.furms.domain.project_installation.ProjectInstallationJob;
import io.imunity.furms.domain.project_installation.ProjectInstallationStatus;
import io.imunity.furms.domain.project_installation.ProjectUpdateJob;
import io.imunity.furms.domain.project_installation.ProjectUpdateStatus;
import io.imunity.furms.domain.projects.Project;
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
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProjectInstallationServiceTest {
	@Mock
	private ProjectOperationRepository repository;
	@Mock
	private SiteAgentProjectOperationService siteAgentProjectOperationService;
	@Mock
	private UsersDAO usersDAO;
	@Mock
	private SiteRepository siteRepository;
	@Mock
	private CommunityRepository communityRepository;

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
		service = new ProjectInstallationServiceImpl(repository, siteAgentProjectOperationService, usersDAO, siteRepository, communityRepository);
		orderVerifier = inOrder(repository, siteAgentProjectOperationService);
	}

	@Test
	void shouldCreateProjectInstallation() {
		//given
		ProjectInstallation projectInstallation = ProjectInstallation.builder().build();

		service.createOrUpdate("projectId", projectInstallation);
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
		when(repository.findProjectInstallation(project.getId())).thenReturn(Set.of(
			ProjectInstallationJob.builder()
				.siteId("siteId")
				.status(ProjectInstallationStatus.INSTALLED)
				.build()
		));
		when(repository.findProjectUpdateStatues(project.getId())).thenReturn(Set.of());
		when(siteRepository.findByProjectId("id")).thenReturn(Set.of(new SiteId("siteId", new SiteExternalId("id"))));

		service.update(project);
		for (TransactionSynchronization transactionSynchronization : TransactionSynchronizationManager
			.getSynchronizations()) {
			transactionSynchronization.afterCommit();
		}

		//then
		orderVerifier.verify(repository).createOrUpdate(any(ProjectUpdateJob.class));

		orderVerifier.verify(siteAgentProjectOperationService).updateProject(any(), any(), any(), any());
	}

	@Test
	void shouldCreateProjectInstallationInsteadOfUpdate() {
		//given
		PersistentId userId = new PersistentId("userId");
		Project project = Project.builder()
			.id("id")
			.communityId("id")
			.leaderId(userId)
			.build();
		FURMSUser user = FURMSUser.builder()
			.id(userId)
			.email("email")
			.build();

		//when
		when(usersDAO.findById(userId)).thenReturn(Optional.of(user));
		when(siteRepository.findByProjectId("id")).thenReturn(Set.of(new SiteId("siteId", new SiteExternalId("id"))));
		when(repository.findProjectInstallation(project.getId())).thenReturn(Set.of(
			ProjectInstallationJob.builder()
				.siteId("siteId")
				.status(ProjectInstallationStatus.FAILED)
				.build()
		));
		when(siteRepository.findByProjectId("id")).thenReturn(Set.of(new SiteId("siteId", new SiteExternalId("id"))));
		when(communityRepository.findById("id")).thenReturn(Optional.of(Community.builder()
			.name("name")
			.build()));

		service.update(project);
		for (TransactionSynchronization transactionSynchronization : TransactionSynchronizationManager
			.getSynchronizations()) {
			transactionSynchronization.afterCommit();
		}

		//then
		orderVerifier.verify(repository).createOrUpdate(any(ProjectInstallationJob.class));
		orderVerifier.verify(siteAgentProjectOperationService).installProject(any(), any());
	}

	@Test
	void shouldNotCreateProjectUpdateWhenProjectIsUpdating() {
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
		when(repository.findProjectInstallation(project.getId())).thenReturn(Set.of(
			ProjectInstallationJob.builder()
				.siteId("siteId")
				.status(ProjectInstallationStatus.INSTALLED)
				.build()
		));
		when(repository.findProjectUpdateStatues(project.getId())).thenReturn(Set.of(ProjectUpdateStatus.ACKNOWLEDGED));

		assertThrows(IllegalStateException.class, () -> service.update(project));
	}
}