/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.core.project_installation;

import io.imunity.furms.domain.project_installation.*;
import io.imunity.furms.domain.projects.Project;
import io.imunity.furms.domain.site_agent.CorrelationId;
import io.imunity.furms.site.api.site_agent.SiteAgentProjectOperationService;
import io.imunity.furms.spi.project_installation.ProjectOperationRepository;
import io.imunity.furms.spi.sites.SiteRepository;
import io.imunity.furms.spi.users.UsersDAO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.lang.invoke.MethodHandles;

@Service
class ProjectInstallationServiceImpl implements ProjectInstallationService {
	private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	private final ProjectOperationRepository projectOperationRepository;
	private final SiteAgentProjectOperationService siteAgentProjectOperationService;
	private final UsersDAO usersDAO;
	private final SiteRepository siteRepository;

	ProjectInstallationServiceImpl(ProjectOperationRepository projectOperationRepository,
	                               SiteAgentProjectOperationService siteAgentProjectOperationService,
	                               UsersDAO usersDAO, SiteRepository siteRepository) {
		this.projectOperationRepository = projectOperationRepository;
		this.siteAgentProjectOperationService = siteAgentProjectOperationService;
		this.usersDAO = usersDAO;
		this.siteRepository = siteRepository;
	}

	@Override
	public ProjectInstallation findProjectInstallation(String projectAllocationId) {
		return projectOperationRepository.findProjectInstallation(projectAllocationId, usersDAO::findById);
	}

	@Override
	public boolean existsByProjectId(String siteId, String projectId) {
		return projectOperationRepository.existsByProjectId(siteId, projectId);
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public void create(String projectId, ProjectInstallation projectInstallation) {
		CorrelationId correlationId = CorrelationId.randomID();
		ProjectInstallationJob projectInstallationJob = ProjectInstallationJob.builder()
			.correlationId(correlationId)
			.siteId(projectInstallation.siteId)
			.projectId(projectId)
			.status(ProjectInstallationStatus.PENDING)
			.build();
		projectOperationRepository.create(projectInstallationJob);
		siteAgentProjectOperationService.installProject(projectInstallationJob.correlationId, projectInstallation);
		LOG.info("ProjectInstallation was updated: {}", projectInstallationJob);
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public void update(Project project) {
		siteRepository.findByProjectId(project.getId()).forEach(siteId -> {
			ProjectUpdateJob projectUpdateJob = ProjectUpdateJob.builder()
				.correlationId(CorrelationId.randomID())
				.siteId(siteId.id)
				.projectId(project.getId())
				.status(ProjectUpdateStatus.PENDING)
				.build();
			projectOperationRepository.create(projectUpdateJob);
			siteAgentProjectOperationService.updateProject(
				projectUpdateJob.correlationId,
				siteId.externalId,
				project,
				usersDAO.findById(project.getLeaderId()).get()
			);
			LOG.info("ProjectUpdateJob was created: {}", projectUpdateJob);
		});
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public void remove(String projectId) {
		siteRepository.findByProjectId(projectId).forEach(siteId -> {
			CorrelationId correlationId = CorrelationId.randomID();
			siteAgentProjectOperationService.removeProject(
				correlationId,
				siteId.externalId,
				projectId
			);
			LOG.info("ProjectRemovalJob was created: {}", correlationId);
		});
	}
}
