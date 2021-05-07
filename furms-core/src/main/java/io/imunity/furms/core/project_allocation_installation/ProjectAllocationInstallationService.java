/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.core.project_allocation_installation;

import io.imunity.furms.domain.project_allocation.ProjectAllocationResolved;
import io.imunity.furms.domain.project_allocation_installation.ProjectAllocationInstallation;
import io.imunity.furms.domain.project_allocation_installation.ProjectAllocationInstallationStatus;
import io.imunity.furms.domain.project_allocation_installation.ProjectDeallocation;
import io.imunity.furms.domain.project_allocation_installation.ProjectDeallocationStatus;
import io.imunity.furms.domain.site_agent.CorrelationId;
import io.imunity.furms.site.api.site_agent.SiteAgentProjectAllocationInstallationService;
import io.imunity.furms.spi.project_allocation.ProjectAllocationRepository;
import io.imunity.furms.spi.project_allocation_installation.ProjectAllocationInstallationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.lang.invoke.MethodHandles;
import java.util.Set;

@Service
public class ProjectAllocationInstallationService {
	private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	private final ProjectAllocationInstallationRepository projectAllocationInstallationRepository;
	private final ProjectAllocationRepository projectAllocationRepository;
	private final SiteAgentProjectAllocationInstallationService siteAgentProjectAllocationInstallationService;

	ProjectAllocationInstallationService(ProjectAllocationInstallationRepository projectAllocationInstallationRepository,
	                                     ProjectAllocationRepository projectAllocationRepository,
	                                     SiteAgentProjectAllocationInstallationService siteAgentProjectAllocationInstallationService) {
		this.projectAllocationInstallationRepository = projectAllocationInstallationRepository;
		this.projectAllocationRepository = projectAllocationRepository;
		this.siteAgentProjectAllocationInstallationService = siteAgentProjectAllocationInstallationService;
	}

	public Set<ProjectAllocationInstallation> findAll(String communityId, String projectId) {
		return projectAllocationInstallationRepository.findAll(projectId);
	}

	public Set<ProjectAllocationInstallation> findAll(String projectId) {
		return projectAllocationInstallationRepository.findAll(projectId);
	}

	public void createAllocation(String projectAllocationId) {
		CorrelationId correlationId = CorrelationId.randomID();
		ProjectAllocationResolved projectAllocationResolved = projectAllocationRepository.findByIdWithRelatedObjects(projectAllocationId).get();
		ProjectAllocationInstallation projectAllocationInstallation = ProjectAllocationInstallation.builder()
			.correlationId(correlationId)
			.siteId(projectAllocationResolved.site.getId())
			.projectAllocationId(projectAllocationId)
			.status(ProjectAllocationInstallationStatus.PROVISIONING_PROJECT)
			.build();
		projectAllocationInstallationRepository.create(projectAllocationInstallation);
		LOG.info("ProjectAllocationInstallation was updated: {}", projectAllocationInstallation);
	}

	public void startWaitingAllocations(String projectId) {
		projectAllocationInstallationRepository.findAll(projectId).forEach(allocation -> {
			projectAllocationInstallationRepository.update(allocation.correlationId.id, ProjectAllocationInstallationStatus.PENDING);
			ProjectAllocationResolved projectAllocationResolved = projectAllocationRepository.findByIdWithRelatedObjects(allocation.projectAllocationId).get();
			siteAgentProjectAllocationInstallationService.allocateProject(allocation.correlationId, projectAllocationResolved);
			LOG.info("ProjectAllocationInstallation with given correlationId {} was updated to: {}", allocation.correlationId.id, ProjectAllocationInstallationStatus.PENDING);
		});
	}

	public void createDeallocation(ProjectAllocationResolved projectAllocationResolved) {
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
