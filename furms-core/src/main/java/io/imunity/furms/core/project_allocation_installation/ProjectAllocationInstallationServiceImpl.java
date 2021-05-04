/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.core.project_allocation_installation;

import io.imunity.furms.core.config.security.method.FurmsAuthorize;
import io.imunity.furms.domain.project_allocation.ProjectAllocationResolved;
import io.imunity.furms.domain.project_allocation_installation.ProjectAllocationInstallation;
import io.imunity.furms.domain.project_allocation_installation.ProjectDeallocation;
import io.imunity.furms.domain.project_allocation_installation.ProjectDeallocationStatus;
import io.imunity.furms.domain.site_agent.CorrelationId;
import io.imunity.furms.site.api.site_agent.SiteAgentProjectAllocationInstallationService;
import io.imunity.furms.spi.project_allocation_installation.ProjectAllocationInstallationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.lang.invoke.MethodHandles;
import java.util.Set;

import static io.imunity.furms.domain.authz.roles.Capability.*;
import static io.imunity.furms.domain.authz.roles.ResourceType.COMMUNITY;
import static io.imunity.furms.domain.authz.roles.ResourceType.PROJECT;

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
	@FurmsAuthorize(capability = COMMUNITY_READ, resourceType = COMMUNITY, id = "communityId")
	public Set<ProjectAllocationInstallation> findAll(String communityId, String projectId) {
		return projectAllocationInstallationRepository.findAll(projectId);
	}

	@Override
	@FurmsAuthorize(capability = PROJECT_READ, resourceType = PROJECT, id = "projectId")
	public Set<ProjectAllocationInstallation> findAll(String projectId) {
		return projectAllocationInstallationRepository.findAll(projectId);
	}

	@Override
	@Transactional
	@FurmsAuthorize(capability = COMMUNITY_WRITE, resourceType = COMMUNITY, id = "communityId")
	public void createAllocation(String communityId, ProjectAllocationInstallation projectAllocationInstallation, ProjectAllocationResolved projectAllocationResolved) {
		projectAllocationInstallationRepository.create(projectAllocationInstallation);
		siteAgentProjectAllocationInstallationService.allocateProject(projectAllocationInstallation.correlationId, projectAllocationResolved);
		LOG.info("ProjectAllocationInstallation was updated: {}", projectAllocationInstallation);
	}

	@Override
	@Transactional
	@FurmsAuthorize(capability = COMMUNITY_WRITE, resourceType = COMMUNITY, id = "communityId")
	public void createDeallocation(String communityId, ProjectAllocationResolved projectAllocationResolved) {
		CorrelationId correlationId = CorrelationId.randomID();
		ProjectDeallocation projectDeallocation = ProjectDeallocation.builder()
			.siteId(projectAllocationResolved.site.getId())
			.correlationId(correlationId)
			.projectAllocationId(projectAllocationResolved.projectId)
			.status(ProjectDeallocationStatus.PENDING)
			.build();
		projectAllocationInstallationRepository.create(projectDeallocation);
		siteAgentProjectAllocationInstallationService.deallocateProject(correlationId, projectAllocationResolved);
		LOG.info("ProjectDeallocation was created: {}", projectDeallocation);
	}
}
