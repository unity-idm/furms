/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.core.project_allocation_installation;

import io.imunity.furms.core.config.security.method.FurmsAuthorize;
import io.imunity.furms.domain.project_allocation.ProjectAllocationResolved;
import io.imunity.furms.domain.project_allocation_installation.ProjectAllocationInstallation;
import io.imunity.furms.site.api.site_agent.SiteAgentProjectAllocationInstallationService;
import io.imunity.furms.spi.project_allocation_installation.ProjectAllocationInstallationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.lang.invoke.MethodHandles;
import java.util.Set;

import static io.imunity.furms.domain.authz.roles.Capability.COMMUNITY_WRITE;
import static io.imunity.furms.domain.authz.roles.ResourceType.COMMUNITY;

@Service
class ProjectAllocationInstallationServiceImpl implements ProjectAllocationInstallationService {
	private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	private final ProjectAllocationInstallationRepository projectAllocationInstallationRepository;
	private final SiteAgentProjectAllocationInstallationService siteAgentProjectAllocationInstallationService;

	ProjectAllocationInstallationServiceImpl(ProjectAllocationInstallationRepository projectAllocationInstallationRepository,
	                                         SiteAgentProjectAllocationInstallationService siteAgentProjectAllocationInstallationService) {
		this.projectAllocationInstallationRepository = projectAllocationInstallationRepository;
		this.siteAgentProjectAllocationInstallationService = siteAgentProjectAllocationInstallationService;
	}

	@Override
	@FurmsAuthorize(capability = COMMUNITY_WRITE, resourceType = COMMUNITY, id = "communityId")
	public Set<ProjectAllocationInstallation> findAll(String communityId, String projectId) {
		return projectAllocationInstallationRepository.findAll(projectId);
	}

	@Override
	@Transactional
	@FurmsAuthorize(capability = COMMUNITY_WRITE, resourceType = COMMUNITY, id = "communityId")
	public void create(String communityId, ProjectAllocationInstallation projectAllocationInstallation, ProjectAllocationResolved projectAllocationResolved) {
		projectAllocationInstallationRepository.create(projectAllocationInstallation);
		siteAgentProjectAllocationInstallationService.allocateProject(projectAllocationInstallation.correlationId, projectAllocationResolved);
		LOG.info("ProjectAllocationInstallation was updated: {}", projectAllocationInstallation);
	}

	@Override
	@FurmsAuthorize(capability = COMMUNITY_WRITE, resourceType = COMMUNITY, id = "communityId")
	public void delete(String communityId, String id) {
		projectAllocationInstallationRepository.delete(id);
		LOG.info("ProjectAllocationInstallation with given ID {} was deleted", id);
	}
}
