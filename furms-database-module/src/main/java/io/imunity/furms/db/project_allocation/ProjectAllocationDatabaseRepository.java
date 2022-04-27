/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.db.project_allocation;

import io.imunity.furms.domain.communities.CommunityId;
import io.imunity.furms.domain.community_allocation.CommunityAllocationId;
import io.imunity.furms.domain.project_allocation.ProjectAllocation;
import io.imunity.furms.domain.project_allocation.ProjectAllocationId;
import io.imunity.furms.domain.project_allocation.ProjectAllocationResolved;
import io.imunity.furms.domain.projects.ProjectId;
import io.imunity.furms.domain.resource_usage.ResourceUsage;
import io.imunity.furms.domain.sites.SiteId;
import io.imunity.furms.spi.project_allocation.ProjectAllocationRepository;
import io.imunity.furms.spi.resource_usage.ResourceUsageRepository;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import static io.imunity.furms.utils.UTCTimeUtils.convertToUTCTime;
import static java.util.Optional.empty;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toSet;

@Repository
class ProjectAllocationDatabaseRepository implements ProjectAllocationRepository {
	private final ProjectAllocationEntityRepository repository;
	private final ProjectAllocationReadEntityRepository readRepository;
	private final ResourceUsageRepository resourceUsageRepository;
	private final ProjectAllocationConverter converter;

	ProjectAllocationDatabaseRepository(ProjectAllocationEntityRepository repository,
	                                    ProjectAllocationReadEntityRepository readRepository,
	                                    ResourceUsageRepository resourceUsageRepository,
	                                    ProjectAllocationConverter converter) {
		this.repository = repository;
		this.readRepository = readRepository;
		this.resourceUsageRepository = resourceUsageRepository;
		this.converter = converter;
	}

	@Override
	public Optional<ProjectAllocation> findById(ProjectAllocationId id) {
		if (isEmpty(id)) {
			return empty();
		}
		return repository.findById(id.id)
			.map(ProjectAllocationEntity::toProjectAllocation);
	}

	@Override
	public Optional<ProjectAllocationResolved> findByIdWithRelatedObjects(ProjectAllocationId id) {
		if (isEmpty(id)) {
			return empty();
		}
		BigDecimal newestResourceUsage = resourceUsageRepository.findCurrentResourceUsage(id)
			.map(usage -> usage.cumulativeConsumption).orElse(BigDecimal.ZERO);
		return readRepository.findById(id.id)
			.map(x -> converter.toProjectAllocationResolved(x, newestResourceUsage));
	}

	@Override
	public Set<ProjectAllocationResolved> findAllWithRelatedObjects(ProjectId projectId) {
		Map<UUID, ResourceUsage> projectAllocationUsage =
			resourceUsageRepository.findCurrentResourceUsages(projectId).stream()
			.collect(toMap(usage -> usage.projectAllocationId.id, Function.identity()));
		return readRepository.findAllByProjectId(projectId.id).stream()
			.map(allocationReadEntity -> converter.toProjectAllocationResolved(
					allocationReadEntity,
					ofNullable(projectAllocationUsage.get(allocationReadEntity.getId()))
						.map(y -> y.cumulativeConsumption)
						.orElse(BigDecimal.ZERO))
			)
			.collect(Collectors.toSet());
	}

	@Override
	public Set<ProjectAllocationResolved> findAllWithRelatedObjects(SiteId siteId, ProjectId projectId) {
		Map<UUID, ResourceUsage> usageByAllocId = resourceUsageRepository.findCurrentResourceUsages(projectId).stream()
			.collect(toMap(resourceUsage -> resourceUsage.projectAllocationId.id, Function.identity()));
		return readRepository.findAllBySiteIdAndProjectId(siteId.id, projectId.id).stream()
			.map(allocationReadEntity -> converter.toProjectAllocationResolved(
				allocationReadEntity,
				ofNullable(usageByAllocId.get(allocationReadEntity.getId()))
					.map(y -> y.cumulativeConsumption)
					.orElse(BigDecimal.ZERO))
			)
			.collect(Collectors.toSet());
	}

	@Override
	public Set<ProjectAllocationResolved> findAllWithRelatedObjectsBySiteId(SiteId siteId) {
		final Set<ProjectAllocationReadEntity> allocations = readRepository.findAllBySiteId(siteId.id);
		final Map<UUID, BigDecimal> projectAllocationUsage = allocations.stream()
				.map(allocation -> allocation.projectId)
				.map(ProjectId::new)
				.distinct()
				.map(resourceUsageRepository::findCurrentResourceUsages)
				.flatMap(Collection::stream)
				.collect(toMap(x -> x.projectAllocationId.id, x -> x.cumulativeConsumption));
		return allocations.stream()
				.map(allocationReadEntity -> converter.toProjectAllocationResolved(
						allocationReadEntity,
						ofNullable(projectAllocationUsage.get(allocationReadEntity.getId()))
								.orElse(BigDecimal.ZERO)))
				.collect(Collectors.toSet());
	}

	@Override
	public Set<ProjectAllocation> findAll(ProjectId projectId) {
		return repository.findAllByProjectId(projectId.id).stream()
			.map(ProjectAllocationEntity::toProjectAllocation)
			.collect(toSet());
	}

	@Override
	public ProjectAllocationId create(ProjectAllocation projectAllocation) {
		ProjectAllocationEntity savedProjectAllocation = repository.save(
			ProjectAllocationEntity.builder()
				.projectId(projectAllocation.projectId.id)
				.communityAllocationId(projectAllocation.communityAllocationId.id)
				.name(projectAllocation.name)
				.amount(projectAllocation.amount)
				.creationTime(convertToUTCTime(ZonedDateTime.now()))
				.build()
		);
		return new ProjectAllocationId(savedProjectAllocation.getId());
	}

	@Override
	public void update(ProjectAllocation projectAllocation) {
		repository.findById(projectAllocation.id.id)
			.map(oldProjectAllocation -> ProjectAllocationEntity.builder()
				.id(oldProjectAllocation.getId())
				.projectId(projectAllocation.projectId.id)
				.communityAllocationId(projectAllocation.communityAllocationId.id)
				.name(projectAllocation.name)
				.amount(projectAllocation.amount)
				.creationTime(oldProjectAllocation.creationTime)
				.build()
			)
			.map(repository::save)
			.map(ProjectAllocationEntity::getId)
			.map(UUID::toString)
			.orElseThrow(() -> new IllegalStateException("Project allocation not found: " + projectAllocation.id));
	}

	@Override
	public BigDecimal getAvailableAmount(CommunityAllocationId communityAllocationId) {
		return readRepository.calculateAvailableAmount(communityAllocationId.id).getAmount();
	}

	@Override
	public boolean exists(ProjectAllocationId id) {
		if (isEmpty(id)) {
			return false;
		}
		return repository.existsById(id.id);
	}

	@Override
	public boolean existsByCommunityAllocationId(CommunityAllocationId id) {
		if (isEmpty(id)) {
			return false;
		}
		return repository.existsByCommunityAllocationId(id.id);
	}

	@Override
	public boolean isNamePresent(CommunityId communityId, String name) {
		return !readRepository.existsByCommunityIdAndName(communityId.id, name);
	}

	@Override
	public void deleteById(ProjectAllocationId id) {
		repository.deleteById(id.id);
	}

	@Override
	public void deleteAll() {
		repository.deleteAll();
	}

	private boolean isEmpty(ProjectAllocationId id) {
		return id == null || id.id == null;
	}

	private boolean isEmpty(CommunityAllocationId id) {
		return id == null || id.id == null;
	}
}

