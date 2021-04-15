/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.core.project_allocation;

import io.imunity.furms.api.project_allocation.ProjectAllocationService;
import io.imunity.furms.api.project_installation.ProjectInstallationService;
import io.imunity.furms.core.config.security.method.FurmsAuthorize;
import io.imunity.furms.domain.project_allocation.*;
import io.imunity.furms.domain.project_installation.ProjectInstallation;
import io.imunity.furms.domain.project_installation.ProjectInstallationJob;
import io.imunity.furms.domain.site_agent.CorrelationId;
import io.imunity.furms.site.api.site_agent.SiteAgentProjectInstallationService;
import io.imunity.furms.spi.project_allocation.ProjectAllocationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.lang.invoke.MethodHandles;
import java.math.BigDecimal;
import java.util.Optional;
import java.util.Set;

import static io.imunity.furms.domain.authz.roles.Capability.*;
import static io.imunity.furms.domain.authz.roles.ResourceType.COMMUNITY;
import static io.imunity.furms.domain.authz.roles.ResourceType.PROJECT;
import static io.imunity.furms.domain.project_installation.ProjectInstallationStatus.SEND;

@Service
class ProjectAllocationServiceImpl implements ProjectAllocationService {
	private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	private final ProjectAllocationRepository projectAllocationRepository;
	private final ProjectInstallationService projectInstallationService;
	private final ProjectAllocationServiceValidator validator;
	private final SiteAgentProjectInstallationService siteAgentProjectInstallationService;
	private final ApplicationEventPublisher publisher;

	ProjectAllocationServiceImpl(ProjectAllocationRepository projectAllocationRepository,
	                             ProjectInstallationService projectInstallationService,
	                             ProjectAllocationServiceValidator validator,
	                             SiteAgentProjectInstallationService siteAgentProjectInstallationService,
	                             ApplicationEventPublisher publisher) {
		this.projectAllocationRepository = projectAllocationRepository;
		this.projectInstallationService = projectInstallationService;
		this.validator = validator;
		this.siteAgentProjectInstallationService = siteAgentProjectInstallationService;
		this.publisher = publisher;
	}

	@Override
	@FurmsAuthorize(capability = PROJECT_READ, resourceType = PROJECT)
	public Optional<ProjectAllocation> findById(String id) {
		return projectAllocationRepository.findById(id);
	}

	@Override
	@FurmsAuthorize(capability = PROJECT_READ, resourceType = PROJECT)
	public Optional<ProjectAllocationResolved> findByIdWithRelatedObjects(String id) {
		return projectAllocationRepository.findByIdWithRelatedObjects(id);
	}

	@Override
	@FurmsAuthorize(capability = PROJECT_READ, resourceType = PROJECT, id = "projectId")
	public BigDecimal getAvailableAmount(String projectId, String communityAllocationId) {
		return projectAllocationRepository.getAvailableAmount(communityAllocationId);
	}

	@Override
	@FurmsAuthorize(capability = COMMUNITY_READ, resourceType = COMMUNITY, id = "communityId")
	public Set<ProjectAllocationResolved> findAllWithRelatedObjects(String communityId) {
		return projectAllocationRepository.findAllWithRelatedObjects(communityId);
	}

	@Override
	@FurmsAuthorize(capability = PROJECT_READ, resourceType = PROJECT)
	public Set<ProjectAllocation> findAll() {
		return projectAllocationRepository.findAll();
	}

	@Override
	@FurmsAuthorize(capability = PROJECT_WRITE, resourceType = PROJECT, id = "projectAllocation.projectId")
	public void create(ProjectAllocation projectAllocation) {
		validator.validateCreate(projectAllocation);
		String id = projectAllocationRepository.create(projectAllocation);
		installProject(projectAllocation, id);
		publisher.publishEvent(new CreateProjectAllocationEvent(projectAllocation.id));
		LOG.info("ProjectAllocation with given ID: {} was created: {}", id, projectAllocation);
	}

	private void installProject(ProjectAllocation projectAllocation, String id) {
		if(projectAllocationRepository.isFirstAllocation(projectAllocation.projectId)) {
			ProjectInstallation projectInstallation = projectInstallationService.findProjectInstallation(id);
			CorrelationId correlationId = siteAgentProjectInstallationService.installProject(projectInstallation);
			projectInstallationService.create(ProjectInstallationJob.builder()
				.correlationId(correlationId)
				.siteId(projectInstallation.siteId)
				.projectId(projectAllocation.projectId)
				.status(SEND)
				.build()
			);
		}
	}

	@Override
	@FurmsAuthorize(capability = PROJECT_WRITE, resourceType = PROJECT, id = "projectAllocation.projectId")
	public void update(ProjectAllocation projectAllocation) {
		validator.validateUpdate(projectAllocation);
		projectAllocationRepository.update(projectAllocation);
		publisher.publishEvent(new UpdateProjectAllocationEvent(projectAllocation.id));
		LOG.info("ProjectAllocation was updated {}", projectAllocation);
	}

	@Override
	@FurmsAuthorize(capability = PROJECT_WRITE, resourceType = PROJECT, id = "projectId")
	public void delete(String projectId, String id) {
		validator.validateDelete(id);
		projectAllocationRepository.delete(id);
		publisher.publishEvent(new RemoveProjectAllocationEvent(id));
		LOG.info("ProjectAllocation with given ID: {} was deleted", id);
	}
}
