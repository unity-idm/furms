/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.db.project_allocation_installation;

import io.imunity.furms.domain.project_allocation_installation.ProjectAllocationInstallation;
import io.imunity.furms.domain.project_allocation_installation.ProjectAllocationInstallationStatus;
import io.imunity.furms.domain.site_agent.CorrelationId;
import io.imunity.furms.spi.project_allocation_installation.ProjectAllocationInstallationRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.springframework.util.StringUtils.isEmpty;

@Repository
class ProjectAllocationInstallationDatabaseRepository implements ProjectAllocationInstallationRepository {
	private final ProjectAllocationInstallationEntityRepository repository;

	ProjectAllocationInstallationDatabaseRepository(ProjectAllocationInstallationEntityRepository repository) {
		this.repository = repository;
	}

	@Override
	public Set<ProjectAllocationInstallation> findAll(String projectId) {
		if (isEmpty(projectId)) {
			return Set.of();
		}
		return repository.findAllByProjectId(UUID.fromString(projectId)).stream()
			.map(ProjectAllocationInstallationEntity::toProjectAllocationInstallation)
			.collect(Collectors.toSet());
	}

	@Override
	public Optional<ProjectAllocationInstallation> findByCorrelationId(CorrelationId correlationId) {
		return repository.findByCorrelationId(UUID.fromString(correlationId.id))
			.map(ProjectAllocationInstallationEntity::toProjectAllocationInstallation);
	}

	@Override
	public String create(ProjectAllocationInstallation projectAllocation) {
		ProjectAllocationInstallationEntity savedProjectAllocation = repository.save(
			ProjectAllocationInstallationEntity.builder()
				.correlationId(UUID.fromString(projectAllocation.correlationId))
				.siteId(UUID.fromString(projectAllocation.siteId))
				.projectAllocationId(UUID.fromString(projectAllocation.projectAllocationId))
				.chunkId(projectAllocation.chunkId)
				.status(projectAllocation.status)
				.build()
		);
		return savedProjectAllocation.getId().toString();
	}

	@Override
	public String update(String id, ProjectAllocationInstallationStatus status) {
		repository.findById(UUID.fromString(id))
			.map(job -> ProjectAllocationInstallationEntity.builder()
				.id(job.getId())
				.correlationId(job.correlationId)
				.siteId(job.siteId)
				.projectAllocationId(job.projectAllocationId)
				.status(status)
				.build())
			.ifPresent(repository::save);
		return id;
	}

	@Override
	public String update(ProjectAllocationInstallation projectAllocation) {
		return repository.findById(UUID.fromString(projectAllocation.id))
			.map(oldProjectAllocation -> ProjectAllocationInstallationEntity.builder()
				.id(oldProjectAllocation.getId())
				.correlationId(UUID.fromString(projectAllocation.correlationId))
				.siteId(UUID.fromString(projectAllocation.siteId))
				.projectAllocationId(UUID.fromString(projectAllocation.projectAllocationId))
				.chunkId(projectAllocation.chunkId)
				.status(projectAllocation.status)
				.build()
			)
			.map(repository::save)
			.map(ProjectAllocationInstallationEntity::getId)
			.map(UUID::toString)
			.get();
	}

	@Override
	public boolean exists(String id) {
		if (isEmpty(id)) {
			return false;
		}
		return repository.existsById(UUID.fromString(id));
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

