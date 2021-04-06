/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.db.project_allocation;

import io.imunity.furms.domain.project_allocation.ProjectAllocation;
import io.imunity.furms.domain.project_allocation.ProjectAllocationResolved;
import io.imunity.furms.spi.project_allocation.ProjectAllocationRepository;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static java.util.Optional.empty;
import static java.util.stream.Collectors.toSet;
import static java.util.stream.StreamSupport.stream;
import static org.springframework.util.StringUtils.isEmpty;

@Repository
class ProjectAllocationDatabaseRepository implements ProjectAllocationRepository {
	private final ProjectAllocationEntityRepository repository;
	private final ProjectAllocationReadEntityRepository readRepository;

	ProjectAllocationDatabaseRepository(ProjectAllocationEntityRepository repository, ProjectAllocationReadEntityRepository readRepository) {
		this.repository = repository;
		this.readRepository = readRepository;
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
		return readRepository.findById(UUID.fromString(id))
			.map(ProjectAllocationReadEntity::toProjectAllocationResolved);
	}

	@Override
	public Set<ProjectAllocationResolved> findAllWithRelatedObjects(String projectId) {
		return readRepository.findAllByProjectId(UUID.fromString(projectId)).stream()
			.map(ProjectAllocationReadEntity::toProjectAllocationResolved)
			.collect(Collectors.toSet());
	}

	@Override
	public Set<ProjectAllocation> findAll() {
		return stream(repository.findAll().spliterator(), false)
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
			.get();
	}

	@Override
	public BigDecimal getAvailableAmount(String communityAllocationId) {
		return readRepository.calculateAvailableAmount(UUID.fromString(communityAllocationId));
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
	public boolean isUniqueName(String name) {
		return !repository.existsByName(name);
	}

	@Override
	public void delete(String id) {
		repository.deleteById(UUID.fromString(id));
	}

	@Override
	public void deleteAll() {
		repository.deleteAll();
	}
}

