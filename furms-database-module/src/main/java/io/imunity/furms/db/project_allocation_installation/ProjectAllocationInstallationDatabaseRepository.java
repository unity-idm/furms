/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.db.project_allocation_installation;

import io.imunity.furms.domain.project_allocation_installation.ErrorMessage;
import io.imunity.furms.domain.project_allocation_installation.ProjectAllocationChunk;
import io.imunity.furms.domain.project_allocation_installation.ProjectAllocationInstallation;
import io.imunity.furms.domain.project_allocation_installation.ProjectAllocationInstallationStatus;
import io.imunity.furms.domain.project_allocation_installation.ProjectDeallocation;
import io.imunity.furms.domain.project_allocation_installation.ProjectDeallocationStatus;
import io.imunity.furms.domain.site_agent.CorrelationId;
import io.imunity.furms.spi.project_allocation_installation.ProjectAllocationInstallationRepository;
import org.springframework.stereotype.Repository;
import org.springframework.util.ObjectUtils;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.springframework.util.ObjectUtils.isEmpty;

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
			throw new IllegalArgumentException("Project Id is empty");
		}
		return allocationRepository.findAllByProjectId(UUID.fromString(projectId)).stream()
			.map(ProjectAllocationInstallationEntity::toProjectAllocationInstallation)
			.collect(Collectors.toSet());
	}

	@Override
	public Set<ProjectAllocationInstallation> findAll(String projectId, String siteId) {
		if (isEmpty(projectId)) {
			throw new IllegalArgumentException("Project Id is empty");
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
	public Optional<ProjectDeallocation> findDeallocationByProjectAllocationId(String projectAllocationId) {
		return deallocationRepository.findByProjectAllocationId(UUID.fromString(projectAllocationId))
			.map(ProjectDeallocationEntity::toProjectDeallocation);
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
	public Set<ProjectAllocationChunk> findAllChunksByAllocationId(String projectAllocationId) {
		if (ObjectUtils.isEmpty(projectAllocationId)) {
			throw new IllegalArgumentException("Project Id is empty");
		}
		return chunkRepository.findAllByProjectAllocationId(UUID.fromString(projectAllocationId)).stream()
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
	public void update(String projectAllocationId, ProjectAllocationInstallationStatus status, CorrelationId correlationId) {
		allocationRepository.findByProjectAllocationId(UUID.fromString(projectAllocationId))
			.map(old -> ProjectAllocationInstallationEntity.builder()
				.id(old.getId())
				.correlationId(UUID.fromString(correlationId.id))
				.siteId(old.siteId)
				.projectAllocationId(old.projectAllocationId)
				.status(status)
				.build())
			.map(allocationRepository::save)
			.map(ProjectAllocationInstallationEntity::getId)
			.map(UUID::toString)
			.orElseThrow(() -> new IllegalArgumentException("Correlation Id not found: " + correlationId));

	}

	@Override
	public void update(String correlationId, ProjectAllocationInstallationStatus status, Optional<ErrorMessage> errorMessage) {
		allocationRepository.findByCorrelationId(UUID.fromString(correlationId))
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
			.orElseThrow(() -> new IllegalArgumentException("Correlation Id not found: " + correlationId));
	}

	@Override
	public ProjectDeallocation findDeallocationByCorrelationId(String correlationId) {
		return deallocationRepository.findByCorrelationId(UUID.fromString(correlationId))
			.map(deallocationEntity -> ProjectDeallocation.builder()
				.siteId(deallocationEntity.siteId.toString())
				.projectAllocationId(deallocationEntity.projectAllocationId.toString())
				.status(ProjectDeallocationStatus.valueOf(deallocationEntity.status))
				.build())
			.orElseThrow(() -> new IllegalArgumentException("Correlation Id not found: " + correlationId));
	}

	@Override
	public void update(String correlationId, ProjectDeallocationStatus status, Optional<ErrorMessage> errorMessage) {
		deallocationRepository.findByCorrelationId(UUID.fromString(correlationId))
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
			.orElseThrow(() -> new IllegalArgumentException("Correlation Id not found: " + correlationId));
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
	public void update(ProjectAllocationChunk projectAllocationChunk) {
		chunkRepository.findByProjectAllocationIdAndChunkId(UUID.fromString(projectAllocationChunk.projectAllocationId) ,projectAllocationChunk.chunkId)
			.map(chunk -> ProjectAllocationChunkEntity.builder()
				.id(chunk.getId())
				.projectAllocationId(UUID.fromString(projectAllocationChunk.projectAllocationId))
				.chunkId(projectAllocationChunk.chunkId)
				.amount(projectAllocationChunk.amount)
				.validFrom(projectAllocationChunk.validFrom)
				.validTo(projectAllocationChunk.validTo)
				.receivedTime(projectAllocationChunk.receivedTime)
				.build())
			.map(chunkRepository::save)
			.orElseThrow(() -> new IllegalArgumentException(String.format("Chunk %s doesn't exist ", projectAllocationChunk.chunkId)));
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

	@Override
	public void delete(CorrelationId id) {
		allocationRepository.deleteByCorrelationId(UUID.fromString(id.id));
		deallocationRepository.deleteByCorrelationId(UUID.fromString(id.id));
	}
}

