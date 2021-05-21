/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.db.project_allocation_installation;

import io.imunity.furms.domain.project_allocation_installation.*;
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
	private final ProjectAllocationInstallationEntityRepository allocationRepository;
	private final ProjectDeallocationEntityRepository deallocationRepository;

	ProjectAllocationInstallationDatabaseRepository(ProjectAllocationInstallationEntityRepository allocationRepository, ProjectDeallocationEntityRepository deallocationRepository) {
		this.allocationRepository = allocationRepository;
		this.deallocationRepository = deallocationRepository;
	}

	@Override
	public Set<ProjectAllocationInstallation> findAll(String projectId) {
		if (isEmpty(projectId)) {
			return Set.of();
		}
		return allocationRepository.findAllByProjectId(UUID.fromString(projectId)).stream()
			.map(ProjectAllocationInstallationEntity::toProjectAllocationInstallation)
			.collect(Collectors.toSet());
	}

	@Override
	public Set<ProjectDeallocation> findAllDeallocation(String projectId) {
		if (isEmpty(projectId)) {
			throw new IllegalArgumentException("Project Id is empty");
		}
		return deallocationRepository.findAllByProjectId(UUID.fromString(projectId)).stream()
			.map(ProjectDeallocationEntity::toProjectDeallocation)
			.collect(Collectors.toSet());
	}

	@Override
	public Optional<ProjectAllocationInstallation> findByCorrelationId(CorrelationId correlationId) {
		return allocationRepository.findByCorrelationId(UUID.fromString(correlationId.id))
			.map(ProjectAllocationInstallationEntity::toProjectAllocationInstallation);
	}

	@Override
	public String create(ProjectAllocationInstallation projectAllocation) {
		ProjectAllocationInstallationEntity savedProjectAllocation = allocationRepository.save(
			ProjectAllocationInstallationEntity.builder()
				.correlationId(UUID.fromString(projectAllocation.correlationId.id))
				.siteId(Optional.ofNullable(projectAllocation.siteId).map(UUID::fromString).orElse(null))
				.projectAllocationId(UUID.fromString(projectAllocation.projectAllocationId))
				.chunkId(projectAllocation.chunkId)
				.amount(projectAllocation.amount)
				.validFrom(projectAllocation.validFrom)
				.validTo(projectAllocation.validTo)
				.receivedTime(projectAllocation.receivedTime)
				.status(projectAllocation.status)
				.build()
		);
		return savedProjectAllocation.getId().toString();
	}

	@Override
	public String create(ProjectDeallocation projectDeallocation) {
		ProjectDeallocationEntity savedProjectAllocation = deallocationRepository.save(
			ProjectDeallocationEntity.builder()
				.correlationId(UUID.fromString(projectDeallocation.correlationId.id))
				.siteId(Optional.ofNullable(projectDeallocation.siteId).map(UUID::fromString).orElse(null))
				.projectAllocationId(UUID.fromString(projectDeallocation.projectAllocationId))
				.status(projectDeallocation.status)
				.build()
		);
		return savedProjectAllocation.getId().toString();
	}

	@Override
	public String update(String correlationId, ProjectAllocationInstallationStatus status, Optional<ErrorMessage> errorMessage) {
		return allocationRepository.findByCorrelationId(UUID.fromString(correlationId))
			.map(installationEntity -> ProjectAllocationInstallationEntity.builder()
				.id(installationEntity.getId())
				.correlationId(installationEntity.correlationId)
				.siteId(installationEntity.siteId)
				.projectAllocationId(installationEntity.projectAllocationId)
				.amount(installationEntity.amount)
				.validFrom(installationEntity.validFrom)
				.validTo(installationEntity.validTo)
				.receivedTime(installationEntity.receivedTime)
				.status(status)
				.code(errorMessage.map(e -> e.code).orElse(null))
				.message(errorMessage.map(e -> e.message).orElse(null))
				.build())
			.map(allocationRepository::save)
			.map(ProjectAllocationInstallationEntity::getId)
			.map(UUID::toString)
			.get();
	}

	@Override
	public ProjectDeallocation findDeallocationByCorrelationId(String correlationId) {
		return deallocationRepository.findByCorrelationId(UUID.fromString(correlationId))
			.map(x -> ProjectDeallocation.builder()
				.projectAllocationId(x.projectAllocationId.toString())
				.status(ProjectDeallocationStatus.valueOf(x.status))
				.build())
			.orElseThrow(() -> new IllegalArgumentException("Correlation Id not found: " + correlationId));
	}

	@Override
	public String update(String correlationId, ProjectDeallocationStatus status, Optional<ErrorMessage> errorMessage) {
		return deallocationRepository.findByCorrelationId(UUID.fromString(correlationId))
			.map(oldProjectAllocation -> ProjectDeallocationEntity.builder()
				.id(oldProjectAllocation.getId())
				.correlationId(oldProjectAllocation.correlationId)
				.siteId(oldProjectAllocation.siteId)
				.projectAllocationId(oldProjectAllocation.projectAllocationId)
				.status(status)
				.code(errorMessage.map(e -> e.code).orElse(null))
				.message(errorMessage.map(e -> e.message).orElse(null))
				.build()
			)
			.map(deallocationRepository::save)
			.map(ProjectDeallocationEntity::getId)
			.map(UUID::toString)
			.get();
	}

	@Override
	public String update(ProjectAllocationInstallation projectAllocation) {
		return allocationRepository.findByCorrelationId(UUID.fromString(projectAllocation.correlationId.id))
			.map(oldProjectAllocation -> ProjectAllocationInstallationEntity.builder()
				.id(oldProjectAllocation.getId())
				.correlationId(oldProjectAllocation.correlationId)
				.siteId(oldProjectAllocation.siteId)
				.projectAllocationId(oldProjectAllocation.projectAllocationId)
				.chunkId(projectAllocation.chunkId)
				.amount(projectAllocation.amount)
				.validFrom(projectAllocation.validFrom)
				.validTo(projectAllocation.validTo)
				.receivedTime(projectAllocation.receivedTime)
				.status(projectAllocation.status)
				.build()
			)
			.map(allocationRepository::save)
			.map(ProjectAllocationInstallationEntity::getId)
			.map(UUID::toString)
			.get();
	}

	@Override
	public boolean exists(String id) {
		if (isEmpty(id)) {
			return false;
		}
		return allocationRepository.existsById(UUID.fromString(id));
	}


	@Override
	public void delete(String id) {
		allocationRepository.deleteById(UUID.fromString(id));
	}

	@Override
	public void deleteAll() {
		allocationRepository.deleteAll();
		deallocationRepository.deleteAll();
	}
}

