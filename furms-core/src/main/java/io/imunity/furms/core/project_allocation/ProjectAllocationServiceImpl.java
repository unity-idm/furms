/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.core.project_allocation;

import static io.imunity.furms.domain.authz.roles.Capability.COMMUNITY_READ;
import static io.imunity.furms.domain.authz.roles.Capability.COMMUNITY_WRITE;
import static io.imunity.furms.domain.authz.roles.Capability.PROJECT_READ;
import static io.imunity.furms.domain.authz.roles.ResourceType.COMMUNITY;
import static io.imunity.furms.domain.authz.roles.ResourceType.PROJECT;

import java.lang.invoke.MethodHandles;
import java.math.BigDecimal;
import java.util.Optional;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.imunity.furms.api.project_allocation.ProjectAllocationService;
import io.imunity.furms.core.config.security.method.FurmsAuthorize;
import io.imunity.furms.core.project_allocation_installation.ProjectAllocationInstallationService;
import io.imunity.furms.core.project_installation.ProjectInstallationService;
import io.imunity.furms.domain.community_allocation.CommunityAllocationResolved;
import io.imunity.furms.domain.project_allocation.CreateProjectAllocationEvent;
import io.imunity.furms.domain.project_allocation.ProjectAllocation;
import io.imunity.furms.domain.project_allocation.ProjectAllocationResolved;
import io.imunity.furms.domain.project_allocation.RemoveProjectAllocationEvent;
import io.imunity.furms.domain.project_allocation.UpdateProjectAllocationEvent;
import io.imunity.furms.domain.project_allocation_installation.ProjectAllocationInstallation;
import io.imunity.furms.domain.project_allocation_installation.ProjectDeallocation;
import io.imunity.furms.domain.project_installation.ProjectInstallation;
import io.imunity.furms.spi.community_allocation.CommunityAllocationRepository;
import io.imunity.furms.spi.project_allocation.ProjectAllocationRepository;

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
		return communityAllocationRepository.findAllByCommunityIdWithRelatedObjects(communityId);
	}

	@Override
	@FurmsAuthorize(capability = COMMUNITY_READ, resourceType = COMMUNITY, id = "communityId")
	public BigDecimal getAvailableAmount(String communityId, String communityAllocationId) {
		validator.validateCommunityIdAndCommunityAllocationId(communityId, communityAllocationId);
		return projectAllocationRepository.getAvailableAmount(communityAllocationId);
	}

	@Override
	@FurmsAuthorize(capability = PROJECT_READ, resourceType = PROJECT, id = "projectId")
	public Set<ProjectAllocationResolved> findAllWithRelatedObjects(String communityId, String projectId) {
		validator.validateCommunityIdAndProjectId(communityId, projectId);
		return projectAllocationRepository.findAllWithRelatedObjects(projectId);
	}

	@Override
	@FurmsAuthorize(capability = COMMUNITY_READ, resourceType = COMMUNITY, id = "communityId")
	public Set<ProjectAllocationInstallation> findAllInstallations(String communityId, String projectId) {
		validator.validateCommunityIdAndProjectId(communityId, projectId);
		return projectAllocationInstallationService.findAll(projectId);
	}

	@Override
	@FurmsAuthorize(capability = COMMUNITY_READ, resourceType = COMMUNITY, id = "communityId")
	public Set<ProjectDeallocation> findAllUninstallations(String communityId, String projectId) {
		validator.validateCommunityIdAndProjectId(communityId, projectId);
		return projectAllocationInstallationService.findAllUninstallation(projectId);
	}

	@Override
	@FurmsAuthorize(capability = PROJECT_READ, resourceType = PROJECT, id = "projectId")
	public Set<ProjectDeallocation> findAllUninstallations(String projectId) {
		return projectAllocationInstallationService.findAllUninstallation(projectId);
	}

	@Override
	@FurmsAuthorize(capability = PROJECT_READ, resourceType = PROJECT, id = "projectId")
	public Set<ProjectAllocationInstallation> findAllInstallations(String projectId) {
		return projectAllocationInstallationService.findAll(projectId);
	}

	@Override
	@FurmsAuthorize(capability = PROJECT_READ, resourceType = PROJECT, id = "projectId")
	public Set<ProjectAllocationResolved> findAllWithRelatedObjects(String projectId) {
		return projectAllocationRepository.findAllWithRelatedObjects(projectId);
	}

	@Override
	@FurmsAuthorize(capability = PROJECT_READ, resourceType = PROJECT, id = "projectId")
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

		allocateProject(projectAllocation, id);

		publisher.publishEvent(new CreateProjectAllocationEvent(projectAllocation.id));
		LOG.info("ProjectAllocation with given ID: {} was created: {}", id, projectAllocation);
	}

	private void allocateProject(ProjectAllocation projectAllocation, String id) {
		ProjectInstallation projectInstallation = projectInstallationService.findProjectInstallation(id);
		if(!projectInstallationService.isProjectInstalled(projectInstallation.siteId, projectAllocation.projectId)) {
			projectInstallationService.create(projectAllocation.projectId, projectInstallation);
			projectAllocationInstallationService.createAllocation(id);
		}
		else {
			projectAllocationInstallationService.createAndStartAllocation(id);
		}
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
		ProjectAllocationResolved projectAllocationResolved = projectAllocationRepository.findByIdWithRelatedObjects(id).get();
		projectAllocationInstallationService.createDeallocation(projectAllocationResolved);
		publisher.publishEvent(new RemoveProjectAllocationEvent(id));
		LOG.info("ProjectAllocation with given ID: {} was deleted", id);
	}
}
