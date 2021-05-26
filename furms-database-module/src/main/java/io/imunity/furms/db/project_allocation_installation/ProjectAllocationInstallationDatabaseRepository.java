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
	private final ProjectAllocationChunkEntityRepository chunkRepository;
	private final ProjectDeallocationEntityRepository deallocationRepository;

	ProjectAllocationInstallationDatabaseRepository(ProjectAllocationInstallationEntityRepository allocationRepository,
	                                                ProjectAllocationChunkEntityRepository chunkRepository,
	                                                ProjectDeallocationEntityRepository deallocationRepository) {
		this.allocationRepository = allocationRepository;
		this.chunkRepository = chunkRepository;
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
	public Set<ProjectAllocationInstallation> findAll(String projectId, String siteId) {
		if (isEmpty(projectId)) {
			return Set.of();
		}
		return allocationRepository.findAllByProjectIdAndSiteId(UUID.fromString(projectId), UUID.fromString(siteId)).stream()
			.map(ProjectAllocationInstallationEntity::toProjectAllocationInstallation)
			.collect(Collectors.toSet());
	}

	@Override
	public ProjectAllocationInstallation findByProjectAllocationId(String projectAllocationId) {
		return allocationRepository.findByProjectAllocationId(UUID.fromString(projectAllocationId))
			.map(ProjectAllocationInstallationEntity::toProjectAllocationInstallation)
			.orElseThrow(() -> new IllegalArgumentException(String.format("Project allocation installation: %s doesn't exist", projectAllocationId)));
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
	public Set<ProjectAllocationChunk> findAllChunks(String projectId) {
		if (isEmpty(projectId)) {
			throw new IllegalArgumentException("Project Id is empty");
		}
		return chunkRepository.findAllByProjectId(UUID.fromString(projectId)).stream()
			.map(ProjectAllocationChunkEntity::toProjectAllocationChunk)
			.collect(Collectors.toSet());
	}

	@Override
	public Optional<ProjectAllocationInstallation> findByCorrelationId(CorrelationId correlationId) {
		return allocationRepository.findByCorrelationId(UUID.fromString(correlationId.id))
			.map(ProjectAllocationInstallationEntity::toProjectAllocationInstallation);
	}

	@Override
	public String create(ProjectAllocationInstallation projectAllocationInstallation) {
		ProjectAllocationInstallationEntity savedProjectAllocation = allocationRepository.save(
			ProjectAllocationInstallationEntity.builder()
				.correlationId(UUID.fromString(projectAllocationInstallation.correlationId.id))
				.siteId(UUID.fromString(projectAllocationInstallation.siteId))
				.projectAllocationId(UUID.fromString(projectAllocationInstallation.projectAllocationId))
				.status(projectAllocationInstallation.status)
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
			.map(old -> ProjectAllocationInstallationEntity.builder()
				.id(old.getId())
				.correlationId(old.correlationId)
				.siteId(old.siteId)
				.projectAllocationId(old.projectAllocationId)
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
	public String create(ProjectAllocationChunk projectAllocationChunk) {
		ProjectAllocationChunkEntity chunk = ProjectAllocationChunkEntity.builder()
			.projectAllocationId(UUID.fromString(projectAllocationChunk.projectAllocationId))
			.chunkId(projectAllocationChunk.chunkId)
			.amount(projectAllocationChunk.amount)
			.validFrom(projectAllocationChunk.validFrom)
			.validTo(projectAllocationChunk.validTo)
			.receivedTime(projectAllocationChunk.receivedTime)
			.build();
		return chunkRepository.save(chunk).getId().toString();
	}

	@Override
	public boolean exists(String id) {
		if (isEmpty(id)) {
			return false;
		}
		return allocationRepository.existsById(UUID.fromString(id));
	}


	@Override
	public void deleteBy(String id) {
		allocationRepository.deleteById(UUID.fromString(id));
	}

	@Override
	public void deleteAll() {
		allocationRepository.deleteAll();
		deallocationRepository.deleteAll();
	}
}

