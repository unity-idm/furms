/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.core.project_installation;

import io.imunity.furms.core.config.security.method.FurmsAuthorize;
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

import static io.imunity.furms.domain.authz.roles.Capability.*;
import static io.imunity.furms.domain.authz.roles.ResourceType.COMMUNITY;
import static io.imunity.furms.domain.authz.roles.ResourceType.PROJECT;

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
	@FurmsAuthorize(capability = COMMUNITY_WRITE, resourceType = COMMUNITY, id = "communityId")
	public ProjectInstallation findProjectInstallation(String communityId, String projectAllocationId) {
		return projectOperationRepository.findProjectInstallation(projectAllocationId, usersDAO::findById);
	}

	@Override
	@FurmsAuthorize(capability = COMMUNITY_WRITE, resourceType = COMMUNITY, id = "communityId")
	public boolean existsByProjectId(String communityId, String projectId) {
		return projectOperationRepository.existsByProjectId(projectId);
	public boolean existsByProjectId(String siteId, String communityId, String projectId) {
		return projectInstallationRepository.existsByProjectId(siteId, projectId);
	}

	@Override
	@FurmsAuthorize(capability = COMMUNITY_WRITE, resourceType = COMMUNITY, id = "communityId")
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public void create(String communityId, String projectId, ProjectInstallation projectInstallation) {
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
	@FurmsAuthorize(capability = PROJECT_LIMITED_WRITE, resourceType = PROJECT, id = "project.id")
	public void update(String communityId, Project project) {
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
	@FurmsAuthorize(capability = PROJECT_WRITE, resourceType = COMMUNITY, id = "communityId")
	public void remove(String communityId, String projectId) {
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
