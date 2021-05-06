/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.core.project_installation;

import io.imunity.furms.core.config.security.method.FurmsAuthorize;
import io.imunity.furms.domain.project_installation.ProjectInstallation;
import io.imunity.furms.domain.project_installation.ProjectInstallationJob;
import io.imunity.furms.site.api.site_agent.SiteAgentProjectInstallationService;
import io.imunity.furms.spi.project_installation.ProjectInstallationRepository;
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

	private final ProjectInstallationRepository projectInstallationRepository;
	private final SiteAgentProjectInstallationService siteAgentProjectInstallationService;
	private final UsersDAO usersDAO;

	ProjectInstallationServiceImpl(ProjectInstallationRepository projectInstallationRepository,
	                               SiteAgentProjectInstallationService siteAgentProjectInstallationService, UsersDAO usersDAO) {
		this.projectInstallationRepository = projectInstallationRepository;
		this.siteAgentProjectInstallationService = siteAgentProjectInstallationService;
		this.usersDAO = usersDAO;
	}

	@Override
	@FurmsAuthorize(capability = COMMUNITY_WRITE, resourceType = COMMUNITY, id = "communityId")
	public ProjectInstallation findProjectInstallation(String communityId, String projectAllocationId) {
		return projectInstallationRepository.findProjectInstallation(projectAllocationId, usersDAO::findById);
	}

	@FurmsAuthorize(capability = COMMUNITY_WRITE, resourceType = COMMUNITY, id = "communityId")
	public boolean existsByProjectId(String siteId, String communityId, String projectId) {
		return projectInstallationRepository.existsByProjectId(siteId, projectId);
	}

	@FurmsAuthorize(capability = COMMUNITY_WRITE, resourceType = COMMUNITY, id = "communityId")
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public void create(String communityId, ProjectInstallationJob projectInstallationJob, ProjectInstallation projectInstallation) {
		projectInstallationRepository.create(projectInstallationJob);
		siteAgentProjectInstallationService.installProject(projectInstallationJob.correlationId, projectInstallation);
		LOG.info("ProjectInstallation was updated: {}", projectInstallationJob);
	}

	@Override
	@FurmsAuthorize(capability = COMMUNITY_WRITE, resourceType = COMMUNITY, id = "communityId")
	public void delete(String communityId, String id) {
		projectInstallationRepository.delete(id);
		LOG.info("ProjectInstallation with given ID {} was deleted", id);
	}
}
