/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.core.project_allocation;

import io.imunity.furms.api.project_allocation.ProjectAllocationService;
import io.imunity.furms.api.validation.exceptions.RemovalOfConsumedProjectAllocationIsFirbiddenException;
import io.imunity.furms.core.config.security.method.FurmsAuthorize;
import io.imunity.furms.core.project_allocation_installation.ProjectAllocationInstallationService;
import io.imunity.furms.core.project_installation.ProjectInstallationService;
import io.imunity.furms.domain.communities.CommunityId;
import io.imunity.furms.domain.community_allocation.CommunityAllocationId;
import io.imunity.furms.domain.project_allocation.ProjectAllocationCreatedEvent;
import io.imunity.furms.domain.project_allocation.ProjectAllocation;
import io.imunity.furms.domain.project_allocation.ProjectAllocationId;
import io.imunity.furms.domain.project_allocation.ProjectAllocationResolved;
import io.imunity.furms.domain.project_allocation.ProjectAllocationRemovedEvent;
import io.imunity.furms.domain.project_allocation.ProjectAllocationUpdatedEvent;
import io.imunity.furms.domain.project_allocation_installation.ProjectAllocationChunk;
import io.imunity.furms.domain.project_allocation_installation.ProjectAllocationChunkResolved;
import io.imunity.furms.domain.project_allocation_installation.ProjectAllocationInstallation;
import io.imunity.furms.domain.project_allocation_installation.ProjectDeallocation;
import io.imunity.furms.domain.project_installation.ProjectInstallation;
import io.imunity.furms.domain.projects.ProjectId;
import io.imunity.furms.domain.sites.SiteId;
import io.imunity.furms.spi.project_allocation.ProjectAllocationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.lang.invoke.MethodHandles;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.Optional;
import java.util.Set;

import static io.imunity.furms.domain.authz.roles.Capability.COMMUNITY_READ;
import static io.imunity.furms.domain.authz.roles.Capability.COMMUNITY_WRITE;
import static io.imunity.furms.domain.authz.roles.Capability.PROJECT_LIMITED_READ;
import static io.imunity.furms.domain.authz.roles.Capability.PROJECT_READ;
import static io.imunity.furms.domain.authz.roles.Capability.SITE_READ;
import static io.imunity.furms.domain.authz.roles.ResourceType.COMMUNITY;
import static io.imunity.furms.domain.authz.roles.ResourceType.PROJECT;
import static io.imunity.furms.domain.authz.roles.ResourceType.SITE;
import static java.util.stream.Collectors.toSet;

@Service
class ProjectAllocationServiceImpl implements ProjectAllocationService {
	private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	private final ProjectAllocationRepository projectAllocationRepository;
	private final ProjectInstallationService projectInstallationService;
	private final ProjectAllocationServiceValidator validator;
	private final ProjectAllocationInstallationService projectAllocationInstallationService;
	private final ApplicationEventPublisher publisher;

	ProjectAllocationServiceImpl(ProjectAllocationRepository projectAllocationRepository,
	                             ProjectInstallationService projectInstallationService,
	                             ProjectAllocationServiceValidator validator,
	                             ProjectAllocationInstallationService projectAllocationInstallationService,
	                             ApplicationEventPublisher publisher) {
		this.projectAllocationRepository = projectAllocationRepository;
		this.projectInstallationService = projectInstallationService;
		this.validator = validator;
		this.projectAllocationInstallationService = projectAllocationInstallationService;
		this.publisher = publisher;
	}

	@Override
	@FurmsAuthorize(capability = PROJECT_READ, resourceType = PROJECT, id = "projectId.id")
	public Optional<ProjectAllocation> findByProjectIdAndId(ProjectId projectId, ProjectAllocationId id) {
		validator.validateProjectIdAndProjectAllocationId(projectId, id);
		return projectAllocationRepository.findById(id);
	}

	@Override
	@FurmsAuthorize(capability = PROJECT_LIMITED_READ, resourceType = PROJECT, id = "projectId.id")
	public Optional<ProjectAllocationResolved> findByIdValidatingProjectsWithRelatedObjects(ProjectAllocationId allocationId,
	                                                                                        ProjectId projectId) {
		validator.validateProjectIdAndProjectAllocationId(projectId, allocationId);
		return projectAllocationRepository.findByIdWithRelatedObjects(allocationId);
	}

	@Override
	@FurmsAuthorize(capability = COMMUNITY_READ, resourceType = COMMUNITY, id = "communityId.id")
	public Optional<ProjectAllocationResolved> findByIdWithRelatedObjects(CommunityId communityId,
	                                                                      ProjectAllocationId id) {
		validator.validateCommunityIdAndProjectAllocationId(communityId, id);
		return projectAllocationRepository.findByIdWithRelatedObjects(id);
	}

	@Override
	@FurmsAuthorize(capability = COMMUNITY_READ, resourceType = COMMUNITY, id = "communityId.id")
	public Set<String> getOccupiedNames(CommunityId communityId, ProjectId id) {
		validator.validateCommunityIdAndProjectId(communityId, id);
		return projectAllocationRepository.findAll(id).stream()
			.map(projectAllocation -> projectAllocation.name)
			.collect(toSet());
	}

	@Override
	@FurmsAuthorize(capability = COMMUNITY_READ, resourceType = COMMUNITY, id = "communityId.id")
	public BigDecimal getAvailableAmount(CommunityId communityId, CommunityAllocationId communityAllocationId) {
		validator.validateCommunityIdAndCommunityAllocationId(communityId, communityAllocationId);
		return projectAllocationRepository.getAvailableAmount(communityAllocationId);
	}

	@Override
	@FurmsAuthorize(capability = PROJECT_READ, resourceType = PROJECT, id = "projectId.id")
	public Set<ProjectAllocationResolved> findAllWithRelatedObjects(CommunityId communityId, ProjectId projectId) {
		validator.validateCommunityIdAndProjectId(communityId, projectId);
		return projectAllocationRepository.findAllWithRelatedObjects(projectId);
	}

	@Override
	@FurmsAuthorize(capability = PROJECT_LIMITED_READ, resourceType = PROJECT, id = "projectId.id")
	public Set<ProjectDeallocation> findAllUninstallations(ProjectId projectId) {
		return projectAllocationInstallationService.findAllUninstallation(projectId);
	}

	@Override
	@FurmsAuthorize(capability = PROJECT_LIMITED_READ, resourceType = PROJECT, id = "projectId.id")
	public Set<ProjectAllocationChunk> findAllChunks(ProjectId projectId) {
		return projectAllocationInstallationService.findAllChunks(projectId);
	}

	@Override
	@FurmsAuthorize(capability = PROJECT_LIMITED_READ, resourceType = PROJECT, id = "projectId.id")
	public Set<ProjectAllocationChunk> findAllChunks(ProjectId projectId, ProjectAllocationId projectAllocationId) {
		validator.validateProjectIdAndProjectAllocationId(projectId, projectAllocationId);
		return projectAllocationInstallationService.findAllChunksByAllocationId(projectAllocationId);
	}

	@Override
	@FurmsAuthorize(capability = SITE_READ, resourceType = SITE, id = "siteId.id")
	public Set<ProjectAllocationChunkResolved> findAllChunksBySiteId(SiteId siteId) {
		final Set<ProjectAllocationResolved> allocations = projectAllocationRepository.findAllWithRelatedObjectsBySiteId(siteId);
		return allocations.stream()
				.map(allocation -> projectAllocationInstallationService.findAllChunks(allocation.projectId))
				.flatMap(Collection::stream)
				.map(chunk -> ProjectAllocationChunkResolved.builder()
						.id(chunk.id)
						.chunkId(chunk.chunkId)
						.projectAllocation(allocations.stream()
								.filter(allocation -> allocation.id.equals(chunk.projectAllocationId))
								.findFirst()
								.orElseThrow(() -> new IllegalArgumentException(
									String.format("This shouldn't happen chunk project allocation id: %s doesn't exist in those allocations: %s", chunk.projectAllocationId, allocations)))
						)
						.amount(chunk.amount)
						.validFrom(chunk.validFrom)
						.validTo(chunk.validTo)
						.receivedTime(chunk.receivedTime)
						.build())
				.collect(toSet());
	}

	@Override
	@FurmsAuthorize(capability = SITE_READ, resourceType = SITE, id = "siteId.id")
	public Set<ProjectAllocationChunkResolved> findAllChunksBySiteIdAndProjectId(SiteId siteId, ProjectId projectId) {
		final Set<ProjectAllocationResolved> allocations = projectAllocationRepository.findAllWithRelatedObjects(projectId);
		return allocations.stream()
				.map(allocation -> projectAllocationInstallationService.findAllChunks(allocation.projectId))
				.flatMap(Collection::stream)
				.map(chunk -> ProjectAllocationChunkResolved.builder()
						.id(chunk.id)
						.chunkId(chunk.chunkId)
						.projectAllocation(allocations.stream()
								.filter(allocation -> allocation.id.equals(chunk.projectAllocationId))
								.findFirst().get())
						.amount(chunk.amount)
						.validFrom(chunk.validFrom)
						.validTo(chunk.validTo)
						.receivedTime(chunk.receivedTime)
						.build())
				.collect(toSet());
	}

	@Override
	@FurmsAuthorize(capability = PROJECT_LIMITED_READ, resourceType = PROJECT, id = "projectId.id")
	public Set<ProjectAllocationInstallation> findAllInstallations(ProjectId projectId) {
		return projectAllocationInstallationService.findAll(projectId);
	}

	@Override
	@FurmsAuthorize(capability = PROJECT_LIMITED_READ, resourceType = PROJECT, id = "projectId.id")
	public Set<ProjectAllocationResolved> findAllWithRelatedObjects(ProjectId projectId) {
		return projectAllocationRepository.findAllWithRelatedObjects(projectId);
	}

	@Override
	@FurmsAuthorize(capability = SITE_READ, resourceType = SITE, id = "siteId.id")
	public Set<ProjectAllocationResolved> findAllWithRelatedObjectsBySiteId(SiteId siteId) {
		return projectAllocationRepository.findAllWithRelatedObjectsBySiteId(siteId);
	}

	@Override
	@FurmsAuthorize(capability = SITE_READ, resourceType = SITE, id = "siteId.id")
	public Set<ProjectAllocationResolved> findAllWithRelatedObjectsBySiteIdAndProjectId(SiteId siteId, ProjectId projectId) {
		return projectAllocationRepository.findAllWithRelatedObjects(projectId).stream()
				.filter(projectAllocation -> projectAllocation.site.getId().equals(siteId))
				.collect(toSet());
	}

	@Override
	@FurmsAuthorize(capability = PROJECT_READ, resourceType = PROJECT, id = "projectId.id")
	public Set<ProjectAllocation> findAll(CommunityId communityId, ProjectId projectId) {
		validator.validateCommunityIdAndProjectId(communityId, projectId);
		return projectAllocationRepository.findAll(projectId);
	}

	@Override
	@Transactional
	@FurmsAuthorize(capability = COMMUNITY_WRITE, resourceType = COMMUNITY, id = "communityId.id")
	public void create(CommunityId communityId, ProjectAllocation projectAllocation) {
		validator.validateCreate(communityId, projectAllocation);
		ProjectAllocationId id = projectAllocationRepository.create(projectAllocation);
		ProjectAllocation created = projectAllocationRepository.findById(id).get();

		allocateProject(projectAllocation, id);

		publisher.publishEvent(new ProjectAllocationCreatedEvent(created));
		LOG.info("ProjectAllocation with given ID: {} was created: {}", id, projectAllocation);
	}

	private void allocateProject(ProjectAllocation projectAllocation, ProjectAllocationId id) {
		ProjectInstallation projectInstallation = projectInstallationService.findProjectInstallationOfProjectAllocation(id);
		if(!projectInstallationService.isProjectInstalled(projectInstallation.siteId, projectAllocation.projectId)) {
			projectInstallationService.createOrUpdate(projectAllocation.projectId, projectInstallation);
			projectAllocationInstallationService.createAllocation(id);
		}
		else {
			projectAllocationInstallationService.createAndStartAllocation(id);
		}
	}

	@Override
	@Transactional
	@FurmsAuthorize(capability = COMMUNITY_WRITE, resourceType = COMMUNITY, id = "communityId.id")
	public void update(CommunityId communityId, ProjectAllocation projectAllocation) {
		validator.validateUpdate(communityId, projectAllocation);
		ProjectAllocation oldProjectAllocation = projectAllocationRepository.findById(projectAllocation.id).get();
		projectAllocationRepository.update(projectAllocation);

		updateProjectAllocation(projectAllocation);

		publisher.publishEvent(new ProjectAllocationUpdatedEvent(oldProjectAllocation, projectAllocation));
		LOG.info("ProjectAllocation was updated {}", projectAllocation);
	}

	private void updateProjectAllocation(ProjectAllocation projectAllocation) {
		ProjectInstallation projectInstallation = projectInstallationService.findProjectInstallationOfProjectAllocation(projectAllocation.id);
		if(!projectInstallationService.isProjectInstalled(projectInstallation.siteId, projectAllocation.projectId))
			projectInstallationService.createOrUpdate(projectAllocation.projectId, projectInstallation);
		else
			projectAllocationInstallationService.updateAndStartAllocation(projectAllocation.id);
	}

	@Override
	@Transactional
	@FurmsAuthorize(capability = COMMUNITY_WRITE, resourceType = COMMUNITY, id = "communityId.id")
	public void delete(CommunityId communityId, ProjectAllocationId id) {
		validator.validateDelete(communityId, id);
		ProjectAllocationResolved projectAllocationResolved = projectAllocationRepository.findByIdWithRelatedObjects(id).get();
		if(projectAllocationResolved.consumed.compareTo(BigDecimal.ZERO) > 0) {
			throw new RemovalOfConsumedProjectAllocationIsFirbiddenException(id);
		}
		projectAllocationInstallationService.createDeallocation(projectAllocationResolved);
		ProjectAllocation projectAllocation = projectAllocationRepository.findById(id).get();
		publisher.publishEvent(new ProjectAllocationRemovedEvent(projectAllocation));
		LOG.info("ProjectAllocation with given ID: {} was deleted", id);
	}
}
