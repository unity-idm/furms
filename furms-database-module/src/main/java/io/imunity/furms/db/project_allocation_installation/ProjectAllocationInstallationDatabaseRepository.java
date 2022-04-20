/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.db.project_allocation_installation;

import io.imunity.furms.domain.project_allocation.ProjectAllocationId;
import io.imunity.furms.domain.project_allocation_installation.ErrorMessage;
import io.imunity.furms.domain.project_allocation_installation.ProjectAllocationChunk;
import io.imunity.furms.domain.project_allocation_installation.ProjectAllocationInstallation;
import io.imunity.furms.domain.project_allocation_installation.ProjectAllocationInstallationId;
import io.imunity.furms.domain.project_allocation_installation.ProjectAllocationInstallationStatus;
import io.imunity.furms.domain.project_allocation_installation.ProjectDeallocation;
import io.imunity.furms.domain.project_allocation_installation.ProjectDeallocationId;
import io.imunity.furms.domain.project_allocation_installation.ProjectDeallocationStatus;
import io.imunity.furms.domain.projects.ProjectId;
import io.imunity.furms.domain.site_agent.CorrelationId;
import io.imunity.furms.domain.sites.SiteId;
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
	public Set<ProjectAllocationInstallation> findAll(ProjectId projectId) {
		if (isEmpty(projectId)) {
			throw new IllegalArgumentException("Project Id is empty");
		}
		return allocationRepository.findAllByProjectId(projectId.id).stream()
			.map(ProjectAllocationInstallationEntity::toProjectAllocationInstallation)
			.collect(Collectors.toSet());
	}

	@Override
	public Set<ProjectAllocationInstallation> findAll(ProjectId projectId, SiteId siteId) {
		if (isEmpty(projectId)) {
			throw new IllegalArgumentException("Project Id is empty");
		}
		return allocationRepository.findAllByProjectIdAndSiteId(projectId.id, siteId.id).stream()
			.map(ProjectAllocationInstallationEntity::toProjectAllocationInstallation)
			.collect(Collectors.toSet());
	}

	@Override
	public ProjectAllocationInstallation findByProjectAllocationId(ProjectAllocationId projectAllocationId) {
		return allocationRepository.findByProjectAllocationId(projectAllocationId.id)
			.map(ProjectAllocationInstallationEntity::toProjectAllocationInstallation)
			.orElseThrow(() -> new IllegalArgumentException(String.format("Project allocation installation: %s doesn't exist", projectAllocationId)));
	}

	@Override
	public Optional<ProjectDeallocation> findDeallocationByProjectAllocationId(ProjectAllocationId projectAllocationId) {
		return deallocationRepository.findByProjectAllocationId(projectAllocationId.id)
			.map(ProjectDeallocationEntity::toProjectDeallocation);
	}

	@Override
	public Set<ProjectDeallocation> findAllDeallocation(ProjectId projectId) {
		if (isEmpty(projectId)) {
			throw new IllegalArgumentException("Project Id is empty");
		}
		return deallocationRepository.findAllByProjectId(projectId.id).stream()
			.map(ProjectDeallocationEntity::toProjectDeallocation)
			.collect(Collectors.toSet());
	}

	@Override
	public Set<ProjectAllocationChunk> findAllChunks(ProjectId projectId) {
		if (isEmpty(projectId)) {
			throw new IllegalArgumentException("Project Id is empty");
		}
		return chunkRepository.findAllByProjectId(projectId.id).stream()
			.map(ProjectAllocationChunkEntity::toProjectAllocationChunk)
			.collect(Collectors.toSet());
	}

	@Override
	public Set<ProjectAllocationChunk> findAllChunksByAllocationId(ProjectAllocationId projectAllocationId) {
		if (ObjectUtils.isEmpty(projectAllocationId)) {
			throw new IllegalArgumentException("Project Id is empty");
		}
		return chunkRepository.findAllByProjectAllocationId(projectAllocationId.id).stream()
			.map(ProjectAllocationChunkEntity::toProjectAllocationChunk)
			.collect(Collectors.toSet());
	}

	@Override
	public Optional<ProjectAllocationInstallation> findByCorrelationId(CorrelationId correlationId) {
		return allocationRepository.findByCorrelationId(UUID.fromString(correlationId.id))
			.map(ProjectAllocationInstallationEntity::toProjectAllocationInstallation);
	}

	@Override
	public ProjectAllocationInstallationId create(ProjectAllocationInstallation projectAllocationInstallation) {
		ProjectAllocationInstallationEntity savedProjectAllocation = allocationRepository.save(
			ProjectAllocationInstallationEntity.builder()
				.correlationId(UUID.fromString(projectAllocationInstallation.correlationId.id))
				.siteId(projectAllocationInstallation.siteId.id)
				.projectAllocationId(projectAllocationInstallation.projectAllocationId.id)
				.status(projectAllocationInstallation.status)
				.build()
		);
		return new ProjectAllocationInstallationId(savedProjectAllocation.getId());
	}

	@Override
	public ProjectDeallocationId create(ProjectDeallocation projectDeallocation) {
		ProjectDeallocationEntity savedProjectAllocation = deallocationRepository.save(
			ProjectDeallocationEntity.builder()
				.correlationId(UUID.fromString(projectDeallocation.correlationId.id))
				.siteId(Optional.ofNullable(projectDeallocation.siteId).map(siteId -> siteId.id).orElse(null))
				.projectAllocationId(projectDeallocation.projectAllocationId.id)
				.status(projectDeallocation.status)
				.build()
		);
		return new ProjectDeallocationId(savedProjectAllocation.getId());
	}

	@Override
	public void update(ProjectAllocationId projectAllocationId, ProjectAllocationInstallationStatus status,
	                   CorrelationId correlationId) {
		allocationRepository.findByProjectAllocationId(projectAllocationId.id)
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
	public void update(CorrelationId correlationId, ProjectAllocationInstallationStatus status,
	                   Optional<ErrorMessage> errorMessage) {
		allocationRepository.findByCorrelationId(UUID.fromString(correlationId.id))
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
	public Optional<ProjectDeallocation> findDeallocationByCorrelationId(CorrelationId correlationId) {
		return deallocationRepository.findByCorrelationId(UUID.fromString(correlationId.id))
			.map(deallocationEntity -> ProjectDeallocation.builder()
				.siteId(deallocationEntity.siteId.toString())
				.projectAllocationId(deallocationEntity.projectAllocationId.toString())
				.status(ProjectDeallocationStatus.valueOf(deallocationEntity.status))
				.build());
	}

	@Override
	public void update(CorrelationId correlationId, ProjectDeallocationStatus status, Optional<ErrorMessage> errorMessage) {
		deallocationRepository.findByCorrelationId(UUID.fromString(correlationId.id))
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
			.projectAllocationId(projectAllocationChunk.projectAllocationId.id)
			.chunkId(projectAllocationChunk.chunkId.id)
			.amount(projectAllocationChunk.amount)
			.validFrom(projectAllocationChunk.validFrom)
			.validTo(projectAllocationChunk.validTo)
			.receivedTime(projectAllocationChunk.receivedTime)
			.build();
		return chunkRepository.save(chunk).getId().toString();
	}

	@Override
	public void update(ProjectAllocationChunk projectAllocationChunk) {
		chunkRepository.findByProjectAllocationIdAndChunkId(projectAllocationChunk.projectAllocationId.id,
				projectAllocationChunk.chunkId.id)
			.map(chunk -> ProjectAllocationChunkEntity.builder()
				.id(chunk.getId())
				.projectAllocationId(projectAllocationChunk.projectAllocationId.id)
				.chunkId(projectAllocationChunk.chunkId.id)
				.amount(projectAllocationChunk.amount)
				.validFrom(projectAllocationChunk.validFrom)
				.validTo(projectAllocationChunk.validTo)
				.receivedTime(projectAllocationChunk.receivedTime)
				.build())
			.map(chunkRepository::save)
			.orElseThrow(() ->
				new IllegalArgumentException(String.format(
					"Chunk %s or project allocation %s don't exist ",
					projectAllocationChunk.chunkId,
					projectAllocationChunk.projectAllocationId))
			);
	}

	@Override
	public boolean exists(ProjectAllocationInstallationId id) {
		if (isEmpty(id)) {
			return false;
		}
		return allocationRepository.existsById(id.id);
	}

	@Override
	public void deleteBy(ProjectAllocationInstallationId id) {
		allocationRepository.deleteById(id.id);
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

