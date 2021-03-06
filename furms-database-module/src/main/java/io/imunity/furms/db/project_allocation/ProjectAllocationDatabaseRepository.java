/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.db.project_allocation;

import io.imunity.furms.domain.project_allocation.ProjectAllocation;
import io.imunity.furms.domain.project_allocation.ProjectAllocationResolved;
import io.imunity.furms.domain.resource_usage.ResourceUsage;
import io.imunity.furms.spi.project_allocation.ProjectAllocationRepository;
import io.imunity.furms.spi.resource_usage.ResourceUsageRepository;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.Optional.empty;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toSet;
import static org.springframework.util.StringUtils.isEmpty;

@Repository
class ProjectAllocationDatabaseRepository implements ProjectAllocationRepository {
	private final ProjectAllocationEntityRepository repository;
	private final ProjectAllocationReadEntityRepository readRepository;
	private final ResourceUsageRepository resourceUsageRepository;

	ProjectAllocationDatabaseRepository(ProjectAllocationEntityRepository repository, ProjectAllocationReadEntityRepository readRepository, ResourceUsageRepository resourceUsageRepository) {
		this.repository = repository;
		this.readRepository = readRepository;
		this.resourceUsageRepository = resourceUsageRepository;
	}

	@Override
	public Optional<ProjectAllocation> findById(String id) {
		if (isEmpty(id)) {
			return empty();
		}
		return repository.findById(UUID.fromString(id))
			.map(ProjectAllocationEntity::toProjectAllocation);
	}

	@Override
	public Optional<ProjectAllocationResolved> findByIdWithRelatedObjects(String id) {
		if (isEmpty(id)) {
			return empty();
		}
		BigDecimal newestResourceUsage = resourceUsageRepository.findCurrentResourceUsage(id)
			.map(usage -> usage.cumulativeConsumption).orElse(BigDecimal.ZERO);
		return readRepository.findById(UUID.fromString(id))
			.map(x -> x.toProjectAllocationResolved(newestResourceUsage));
	}

	@Override
	public Set<ProjectAllocationResolved> findAllWithRelatedObjects(String projectId) {
		Map<String, ResourceUsage> projectAllocationUsage = resourceUsageRepository.findCurrentResourceUsages(projectId).stream()
			.collect(toMap(x -> x.projectAllocationId, Function.identity()));
		return readRepository.findAllByProjectId(UUID.fromString(projectId)).stream()
			.map(allocationReadEntity -> allocationReadEntity.toProjectAllocationResolved(
				ofNullable(projectAllocationUsage.get(allocationReadEntity.getId().toString()))
				.map(y -> y.cumulativeConsumption)
				.orElse(BigDecimal.ZERO))
			)
			.collect(Collectors.toSet());
	}

	@Override
	public Set<ProjectAllocation> findAll(String projectId) {
		return repository.findAllByProjectId(UUID.fromString(projectId)).stream()
			.map(ProjectAllocationEntity::toProjectAllocation)
			.collect(toSet());
	}

	@Override
	public String create(ProjectAllocation projectAllocation) {
		ProjectAllocationEntity savedProjectAllocation = repository.save(
			ProjectAllocationEntity.builder()
				.projectId(UUID.fromString(projectAllocation.projectId))
				.communityAllocationId(UUID.fromString(projectAllocation.communityAllocationId))
				.name(projectAllocation.name)
				.amount(projectAllocation.amount)
				.build()
		);
		return savedProjectAllocation.getId().toString();
	}

	@Override
	public String update(ProjectAllocation projectAllocation) {
		return repository.findById(UUID.fromString(projectAllocation.id))
			.map(oldProjectAllocation -> ProjectAllocationEntity.builder()
				.id(oldProjectAllocation.getId())
				.projectId(UUID.fromString(projectAllocation.projectId))
				.communityAllocationId(UUID.fromString(projectAllocation.communityAllocationId))
				.name(projectAllocation.name)
				.amount(projectAllocation.amount)
				.build()
			)
			.map(repository::save)
			.map(ProjectAllocationEntity::getId)
			.map(UUID::toString)
			.orElseThrow(() -> new IllegalStateException("Project allocation not found: " + projectAllocation.id));
	}

	@Override
	public BigDecimal getAvailableAmount(String communityAllocationId) {
		return readRepository.calculateAvailableAmount(UUID.fromString(communityAllocationId)).getAmount();
	}

	@Override
	public boolean exists(String id) {
		if (isEmpty(id)) {
			return false;
		}
		return repository.existsById(UUID.fromString(id));
	}

	@Override
	public boolean existsByCommunityAllocationId(String id) {
		if (isEmpty(id)) {
			return false;
		}
		return repository.existsByCommunityAllocationId(UUID.fromString(id));
	}

	@Override
	public boolean isNamePresent(String communityId, String name) {
		return !readRepository.existsByCommunityIdAndName(UUID.fromString(communityId), name);
	}

	@Override
	public void deleteById(String id) {
		repository.deleteById(UUID.fromString(id));
	}

	@Override
	public void deleteAll() {
		repository.deleteAll();
	}
}

