/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.core.project_installation;

import io.imunity.furms.core.config.security.method.FurmsAuthorize;
import io.imunity.furms.domain.project_installation.*;
import io.imunity.furms.domain.site_agent.CorrelationId;
import io.imunity.furms.domain.sites.SiteExternalId;
import io.imunity.furms.site.api.site_agent.SiteAgentProjectOperationService;
import io.imunity.furms.spi.project_installation.ProjectOperationRepository;
import io.imunity.furms.spi.users.UsersDAO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.lang.invoke.MethodHandles;

import static io.imunity.furms.domain.authz.roles.Capability.COMMUNITY_WRITE;
import static io.imunity.furms.domain.authz.roles.ResourceType.COMMUNITY;

@Service
class ProjectInstallationServiceImpl implements ProjectInstallationService {
	private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	private final ProjectOperationRepository projectOperationRepository;
	private final SiteAgentProjectOperationService siteAgentProjectOperationService;
	private final UsersDAO usersDAO;

	ProjectInstallationServiceImpl(ProjectOperationRepository projectOperationRepository,
	                               SiteAgentProjectOperationService siteAgentProjectOperationService, UsersDAO usersDAO) {
		this.projectOperationRepository = projectOperationRepository;
		this.siteAgentProjectOperationService = siteAgentProjectOperationService;
		this.usersDAO = usersDAO;
	}

	@Override
	@FurmsAuthorize(capability = COMMUNITY_WRITE, resourceType = COMMUNITY, id = "communityId")
	public ProjectInstallation findProjectInstallation(String communityId, String projectAllocationId) {
		return projectOperationRepository.findProjectInstallation(projectAllocationId, usersDAO::findById);
	}

	@FurmsAuthorize(capability = COMMUNITY_WRITE, resourceType = COMMUNITY, id = "communityId")
	public boolean existsByProjectId(String communityId, String projectId) {
		return projectOperationRepository.existsByProjectId(projectId);
	}

	@FurmsAuthorize(capability = COMMUNITY_WRITE, resourceType = COMMUNITY, id = "communityId")
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public void create(String communityId, String projectId) {
		ProjectInstallation projectInstallation = findProjectInstallation(communityId, projectId);
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
	@Transactional
	public void update(String communityId, String projectId) {
		ProjectInstallation projectInstallation = findProjectInstallation(communityId, projectId);
		CorrelationId correlationId = CorrelationId.randomID();
		ProjectUpdateJob projectUpdateJob = ProjectUpdateJob.builder()
			.correlationId(correlationId)
			.siteId(projectInstallation.siteId)
			.projectId(projectId)
			.status(ProjectUpdateStatus.PENDING)
			.build();
		projectOperationRepository.create(projectUpdateJob);
		siteAgentProjectOperationService.updateProject(projectUpdateJob.correlationId, projectInstallation);
		LOG.info("ProjectUpdate was updated: {}", projectUpdateJob);
	}

	@Override
	@Transactional
	public void remove(String communityId, String projectId) {
		ProjectInstallation projectInstallation = findProjectInstallation(communityId, projectId);
		CorrelationId correlationId = CorrelationId.randomID();
		ProjectRemovalJob projectRemovalJob = ProjectRemovalJob.builder()
			.correlationId(correlationId)
			.siteId(projectInstallation.siteId)
			.projectId(projectId)
			.status(ProjectRemovalStatus.PENDING)
			.build();
		projectOperationRepository.create(projectRemovalJob);
		siteAgentProjectOperationService.removeProject(
			projectRemovalJob.correlationId,
			new SiteExternalId(projectInstallation.siteExternalId),
			projectRemovalJob.projectId
		);
		LOG.info("ProjectRemoval was updated: {}", projectRemovalJob);
	}
}
