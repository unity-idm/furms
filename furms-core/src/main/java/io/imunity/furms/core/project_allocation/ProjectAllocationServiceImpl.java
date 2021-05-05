/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.core.project_allocation;

import io.imunity.furms.api.project_allocation.ProjectAllocationService;
import io.imunity.furms.core.config.security.method.FurmsAuthorize;
import io.imunity.furms.core.project_allocation_installation.ProjectAllocationInstallationService;
import io.imunity.furms.core.project_installation.ProjectInstallationService;
import io.imunity.furms.domain.community_allocation.CommunityAllocationResolved;
import io.imunity.furms.domain.project_allocation.*;
import io.imunity.furms.domain.project_allocation_installation.ProjectAllocationInstallation;
import io.imunity.furms.domain.project_allocation_installation.ProjectAllocationInstallationStatus;
import io.imunity.furms.domain.project_installation.ProjectInstallation;
import io.imunity.furms.domain.site_agent.CorrelationId;
import io.imunity.furms.spi.community_allocation.CommunityAllocationRepository;
import io.imunity.furms.spi.project_allocation.ProjectAllocationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.lang.invoke.MethodHandles;
import java.math.BigDecimal;
import java.util.Optional;
import java.util.Set;

import static io.imunity.furms.domain.authz.roles.Capability.*;
import static io.imunity.furms.domain.authz.roles.ResourceType.COMMUNITY;
import static io.imunity.furms.domain.authz.roles.ResourceType.PROJECT;

@Service
class ProjectAllocationServiceImpl implements ProjectAllocationService {
	private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	private final ProjectAllocationRepository projectAllocationRepository;
	private final CommunityAllocationRepository communityAllocationRepository;
	private final ProjectInstallationService projectInstallationService;
	private final ProjectAllocationServiceValidator validator;
	private final ProjectAllocationInstallationService projectAllocationInstallationService;
	private final ApplicationEventPublisher publisher;

	ProjectAllocationServiceImpl(ProjectAllocationRepository projectAllocationRepository,
	                             ProjectInstallationService projectInstallationService,
	                             CommunityAllocationRepository communityAllocationRepository,
	                             ProjectAllocationServiceValidator validator,
	                             ProjectAllocationInstallationService projectAllocationInstallationService,
	                             ApplicationEventPublisher publisher) {
		this.projectAllocationRepository = projectAllocationRepository;
		this.communityAllocationRepository = communityAllocationRepository;
		this.projectInstallationService = projectInstallationService;
		this.validator = validator;
		this.projectAllocationInstallationService = projectAllocationInstallationService;
		this.publisher = publisher;
	}

	@Override
	@FurmsAuthorize(capability = COMMUNITY_READ, resourceType = COMMUNITY, id = "communityId")
	public Optional<ProjectAllocation> findByCommunityIdAndId(String communityId, String id) {
		validator.validateCommunityIdAndProjectAllocationId(communityId, id);
		return projectAllocationRepository.findById(id);
	}

	@Override
	@FurmsAuthorize(capability = PROJECT_READ, resourceType = PROJECT, id = "projectId")
	public Optional<ProjectAllocation> findByProjectIdAndId(String projectId, String id) {
		validator.validateProjectIdAndProjectAllocationId(projectId, id);
		return projectAllocationRepository.findById(id);
	}

	@Override
	@FurmsAuthorize(capability = COMMUNITY_READ, resourceType = COMMUNITY, id = "communityId")
	public Optional<ProjectAllocationResolved> findByIdWithRelatedObjects(String communityId, String id) {
		validator.validateCommunityIdAndProjectAllocationId(communityId, id);
		return projectAllocationRepository.findByIdWithRelatedObjects(id);
	}

	@Override
	@FurmsAuthorize(capability = COMMUNITY_READ, resourceType = COMMUNITY, id = "communityId")
	public Set<CommunityAllocationResolved> findCorrelatedCommunityAllocation(String communityId) {
		return communityAllocationRepository.findAllWithRelatedObjects(communityId);
	}

	@Override
	@FurmsAuthorize(capability = COMMUNITY_READ, resourceType = COMMUNITY, id = "communityId")
	public BigDecimal getAvailableAmount(String communityId, String communityAllocationId) {
		validator.validateCommunityIdAndCommunityAllocationId(communityId, communityAllocationId);
		return projectAllocationRepository.getAvailableAmount(communityAllocationId);
	}

	@Override
	@FurmsAuthorize(capability = COMMUNITY_READ, resourceType = COMMUNITY, id = "communityId")
	public Set<ProjectAllocationResolved> findAllWithRelatedObjects(String communityId, String projectId) {
		validator.validateCommunityIdAndProjectId(communityId, projectId);
		return projectAllocationRepository.findAllWithRelatedObjects(projectId);
	}

	@Override
	@FurmsAuthorize(capability = COMMUNITY_READ, resourceType = COMMUNITY, id = "communityId")
	public Set<ProjectAllocationInstallation> findAllInstallations(String communityId, String projectId) {
		validator.validateCommunityIdAndProjectId(communityId, projectId);
		return projectAllocationInstallationService.findAll(communityId, projectId);
	}

	@Override
	@FurmsAuthorize(capability = PROJECT_READ, resourceType = PROJECT, id = "projectId")
	public Set<ProjectAllocationResolved> findAllWithRelatedObjects(String projectId) {
		return projectAllocationRepository.findAllWithRelatedObjects(projectId);
	}

	@Override
	@FurmsAuthorize(capability = COMMUNITY_READ, resourceType = COMMUNITY, id = "communityId")
	public Set<ProjectAllocation> findAll(String communityId, String projectId) {
		validator.validateCommunityIdAndProjectId(communityId, projectId);
		return projectAllocationRepository.findAll(projectId);
	}

	@Override
	@Transactional
	@FurmsAuthorize(capability = COMMUNITY_WRITE, resourceType = COMMUNITY, id = "communityId")
	public void create(String communityId, ProjectAllocation projectAllocation) {
		validator.validateCreate(communityId, projectAllocation);
		String id = projectAllocationRepository.create(projectAllocation);

		installProject(projectAllocation, communityId, id);
		allocateProject(communityId, id);

		publisher.publishEvent(new CreateProjectAllocationEvent(projectAllocation.id));
		LOG.info("ProjectAllocation with given ID: {} was created: {}", id, projectAllocation);
	}

	private void installProject(ProjectAllocation projectAllocation, String communityId, String id) {
		if(!projectInstallationService.existsByProjectId(communityId, projectAllocation.projectId)) {
			ProjectInstallation projectInstallation = projectInstallationService.findProjectInstallation(communityId, id);
			projectInstallationService.create(communityId, projectAllocation.projectId, projectInstallation);
		}
	}

	private void allocateProject(String communityId, String projectAllocationId) {
		CorrelationId correlationId = CorrelationId.randomID();
		ProjectAllocationResolved projectAllocationResolved = projectAllocationRepository.findByIdWithRelatedObjects(projectAllocationId).get();
		ProjectAllocationInstallation projectAllocationInstallation = ProjectAllocationInstallation.builder()
			.correlationId(correlationId)
			.siteId(projectAllocationResolved.site.getId())
			.projectAllocationId(projectAllocationId)
			.status(ProjectAllocationInstallationStatus.SENT)
			.build();
		projectAllocationInstallationService.create(communityId, projectAllocationInstallation, projectAllocationResolved);
	}

	@Override
	@Transactional
	@FurmsAuthorize(capability = COMMUNITY_WRITE, resourceType = COMMUNITY, id = "communityId")
	public void update(String communityId, ProjectAllocation projectAllocation) {
		validator.validateUpdate(communityId, projectAllocation);
		projectAllocationRepository.update(projectAllocation);
		publisher.publishEvent(new UpdateProjectAllocationEvent(projectAllocation.id));
		LOG.info("ProjectAllocation was updated {}", projectAllocation);
	}

	@Override
	@Transactional
	@FurmsAuthorize(capability = COMMUNITY_WRITE, resourceType = COMMUNITY, id = "communityId")
	public void delete(String communityId, String id) {
		validator.validateDelete(communityId, id);
		projectAllocationRepository.delete(id);
		publisher.publishEvent(new RemoveProjectAllocationEvent(id));
		LOG.info("ProjectAllocation with given ID: {} was deleted", id);
	}
}
