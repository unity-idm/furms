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
import io.imunity.furms.domain.project_allocation.CreateProjectAllocationEvent;
import io.imunity.furms.domain.project_allocation.ProjectAllocation;
import io.imunity.furms.domain.project_allocation.ProjectAllocationResolved;
import io.imunity.furms.domain.project_allocation.RemoveProjectAllocationEvent;
import io.imunity.furms.domain.project_allocation.UpdateProjectAllocationEvent;
import io.imunity.furms.domain.project_allocation_installation.ProjectAllocationChunk;
import io.imunity.furms.domain.project_allocation_installation.ProjectAllocationChunkResolved;
import io.imunity.furms.domain.project_allocation_installation.ProjectAllocationInstallation;
import io.imunity.furms.domain.project_allocation_installation.ProjectDeallocation;
import io.imunity.furms.domain.project_installation.ProjectInstallation;
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
	@FurmsAuthorize(capability = PROJECT_READ, resourceType = PROJECT, id = "projectId")
	public Optional<ProjectAllocation> findByProjectIdAndId(String projectId, String id) {
		validator.validateProjectIdAndProjectAllocationId(projectId, id);
		return projectAllocationRepository.findById(id);
	}

	@Override
	@FurmsAuthorize(capability = PROJECT_LIMITED_READ, resourceType = PROJECT, id = "projectId")
	public Optional<ProjectAllocationResolved> findByIdValidatingProjectsWithRelatedObjects(String allocationId,
	                                                                                        String projectId) {
		validator.validateProjectIdAndProjectAllocationId(projectId, allocationId);
		return projectAllocationRepository.findByIdWithRelatedObjects(allocationId);
	}

	@Override
	@FurmsAuthorize(capability = COMMUNITY_READ, resourceType = COMMUNITY, id = "communityId")
	public Optional<ProjectAllocationResolved> findByIdWithRelatedObjects(String communityId, String id) {
		validator.validateCommunityIdAndProjectAllocationId(communityId, id);
		return projectAllocationRepository.findByIdWithRelatedObjects(id);
	}

	@Override
	@FurmsAuthorize(capability = COMMUNITY_READ, resourceType = COMMUNITY, id = "communityId")
	public Set<String> getOccupiedNames(String communityId, String id) {
		validator.validateCommunityIdAndProjectId(communityId, id);
		return projectAllocationRepository.findAll(id).stream()
			.map(projectAllocation -> projectAllocation.name)
			.collect(toSet());
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
	@FurmsAuthorize(capability = PROJECT_LIMITED_READ, resourceType = PROJECT, id = "projectId")
	public Set<ProjectDeallocation> findAllUninstallations(String projectId) {
		return projectAllocationInstallationService.findAllUninstallation(projectId);
	}

	@Override
	@FurmsAuthorize(capability = PROJECT_LIMITED_READ, resourceType = PROJECT, id = "projectId")
	public Set<ProjectAllocationChunk> findAllChunks(String projectId) {
		return projectAllocationInstallationService.findAllChunks(projectId);
	}

	@Override
	@FurmsAuthorize(capability = PROJECT_LIMITED_READ, resourceType = PROJECT, id = "projectId")
	public Set<ProjectAllocationChunk> findAllChunks(String projectId, String projectAllocationId) {
		return projectAllocationInstallationService.findAllChunksByAllocationId(projectAllocationId);
	}

	@Override
	@FurmsAuthorize(capability = SITE_READ, resourceType = SITE, id = "siteId")
	public Set<ProjectAllocationChunkResolved> findAllChunksBySiteId(String siteId) {
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
	@FurmsAuthorize(capability = SITE_READ, resourceType = SITE, id = "siteId")
	public Set<ProjectAllocationChunkResolved> findAllChunksBySiteIdAndProjectId(String siteId, String projectId) {
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
	@FurmsAuthorize(capability = PROJECT_LIMITED_READ, resourceType = PROJECT, id = "projectId")
	public Set<ProjectAllocationInstallation> findAllInstallations(String projectId) {
		return projectAllocationInstallationService.findAll(projectId);
	}

	@Override
	@FurmsAuthorize(capability = PROJECT_LIMITED_READ, resourceType = PROJECT, id = "projectId")
	public Set<ProjectAllocationResolved> findAllWithRelatedObjects(String projectId) {
		return projectAllocationRepository.findAllWithRelatedObjects(projectId);
	}

	@Override
	@FurmsAuthorize(capability = SITE_READ, resourceType = SITE, id = "siteId")
	public Set<ProjectAllocationResolved> findAllWithRelatedObjectsBySiteId(String siteId) {
		return projectAllocationRepository.findAllWithRelatedObjectsBySiteId(siteId);
	}

	@Override
	@FurmsAuthorize(capability = SITE_READ, resourceType = SITE, id = "siteId")
	public Set<ProjectAllocationResolved> findAllWithRelatedObjectsBySiteIdAndProjectId(String siteId, String projectId) {
		return projectAllocationRepository.findAllWithRelatedObjects(projectId).stream()
				.filter(projectAllocation -> projectAllocation.site.getId().equals(siteId))
				.collect(toSet());
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
	@FurmsAuthorize(capability = COMMUNITY_WRITE, resourceType = COMMUNITY, id = "communityId")
	public void update(String communityId, ProjectAllocation projectAllocation) {
		validator.validateUpdate(communityId, projectAllocation);
		projectAllocationRepository.update(projectAllocation);

		updateProjectAllocation(projectAllocation);

		publisher.publishEvent(new UpdateProjectAllocationEvent(projectAllocation.id));
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
	@FurmsAuthorize(capability = COMMUNITY_WRITE, resourceType = COMMUNITY, id = "communityId")
	public void delete(String communityId, String id) {
		validator.validateDelete(communityId, id);
		ProjectAllocationResolved projectAllocationResolved = projectAllocationRepository.findByIdWithRelatedObjects(id).get();
		if(projectAllocationResolved.consumed.compareTo(BigDecimal.ZERO) > 0) {
			throw new RemovalOfConsumedProjectAllocationIsFirbiddenException(id);
		}
		projectAllocationInstallationService.createDeallocation(projectAllocationResolved);
		publisher.publishEvent(new RemoveProjectAllocationEvent(id));
		LOG.info("ProjectAllocation with given ID: {} was deleted", id);
	}
}
