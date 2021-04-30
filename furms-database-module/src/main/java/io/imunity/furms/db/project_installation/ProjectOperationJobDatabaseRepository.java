/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.db.project_installation;

import io.imunity.furms.domain.project_installation.*;
import io.imunity.furms.domain.site_agent.CorrelationId;
import io.imunity.furms.domain.users.FURMSUser;
import io.imunity.furms.domain.users.PersistentId;
import io.imunity.furms.spi.project_installation.ProjectOperationRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;

@Repository
class ProjectOperationJobDatabaseRepository implements ProjectOperationRepository {
	private final ProjectInstallationJobEntityRepository installationRepository;
	private final ProjectUpdateJobEntityRepository updateRepository;
	private final ProjectRemovalJobEntityRepository removalRepository;

	ProjectOperationJobDatabaseRepository(ProjectInstallationJobEntityRepository installationRepository,
	                                      ProjectUpdateJobEntityRepository updateRepository, ProjectRemovalJobEntityRepository removalRepository) {
		this.installationRepository = installationRepository;
		this.updateRepository = updateRepository;
		this.removalRepository = removalRepository;
	}

	@Override
	public ProjectInstallationJob findInstallationJobByCorrelationId(CorrelationId correlationId) {
		ProjectInstallationJobEntity job = installationRepository.findByCorrelationId(UUID.fromString(correlationId.id));
		return ProjectInstallationJob.builder()
			.id(job.getId().toString())
			.correlationId(new CorrelationId(job.correlationId.toString()))
			.siteId(job.siteId.toString())
			.projectId(job.projectId.toString())
			.status(ProjectInstallationStatus.valueOf(job.status))
			.build();
	}

	@Override
	public ProjectUpdateJob findUpdateJobByCorrelationId(CorrelationId correlationId) {
		ProjectUpdateJobEntity job = updateRepository.findByCorrelationId(UUID.fromString(correlationId.id));
		return ProjectUpdateJob.builder()
			.id(job.getId().toString())
			.correlationId(new CorrelationId(job.correlationId.toString()))
			.siteId(job.siteId.toString())
			.projectId(job.projectId.toString())
			.status(ProjectUpdateStatus.valueOf(job.status))
			.build();
	}

	@Override
	public ProjectRemovalJob findRemovalJobByCorrelationId(CorrelationId correlationId) {
		ProjectRemovalJobEntity job = removalRepository.findByCorrelationId(UUID.fromString(correlationId.id));
		return ProjectRemovalJob.builder()
			.id(job.getId().toString())
			.correlationId(new CorrelationId(job.correlationId.toString()))
			.siteId(job.siteId.toString())
			.projectId(job.projectId.toString())
			.status(ProjectRemovalStatus.valueOf(job.status))
			.build();
	}

	@Override
	public ProjectInstallation findProjectInstallation(String projectAllocationId, Function<PersistentId, Optional<FURMSUser>> userGetter){
		ProjectInstallationEntity allocation = installationRepository.findByProjectAllocationId(UUID.fromString(projectAllocationId));
		return ProjectInstallation.builder()
			.id(allocation.id)
			.siteId(allocation.siteId)
			.siteExternalId(allocation.siteExternalId)
			.name(allocation.name)
			.description(allocation.description)
			.communityId(allocation.communityId)
			.communityName(allocation.communityName)
			.acronym(allocation.acronym)
			.researchField(allocation.researchField)
			.validityStart(allocation.validityStart)
			.validityEnd(allocation.validityEnd)
			.leader(userGetter.apply(new PersistentId(allocation.leaderId)).orElse(null))
			.build();
	}

	@Override
	public String create(ProjectInstallationJob projectInstallationJob) {
		ProjectInstallationJobEntity projectInstallationJobEntity = ProjectInstallationJobEntity.builder()
			.correlationId(UUID.fromString(projectInstallationJob.correlationId.id))
			.siteId(UUID.fromString(projectInstallationJob.siteId))
			.projectId(UUID.fromString(projectInstallationJob.projectId))
			.status(projectInstallationJob.status)
			.build();
		ProjectInstallationJobEntity job = installationRepository.save(projectInstallationJobEntity);
		return job.getId().toString();
	}

	@Override
	public String create(ProjectUpdateJob projectUpdateJob) {
		ProjectUpdateJobEntity projectUpdateJobEntity = ProjectUpdateJobEntity.builder()
			.correlationId(UUID.fromString(projectUpdateJob.correlationId.id))
			.siteId(UUID.fromString(projectUpdateJob.siteId))
			.projectId(UUID.fromString(projectUpdateJob.projectId))
			.status(projectUpdateJob.status)
			.build();
		ProjectUpdateJobEntity job = updateRepository.save(projectUpdateJobEntity);
		return job.getId().toString();
	}

	@Override
	public String create(ProjectRemovalJob projectRemovalJob) {
		ProjectRemovalJobEntity projectRemovalJobEntity = ProjectRemovalJobEntity.builder()
			.correlationId(UUID.fromString(projectRemovalJob.correlationId.id))
			.siteId(UUID.fromString(projectRemovalJob.siteId))
			.projectId(UUID.fromString(projectRemovalJob.projectId))
			.status(projectRemovalJob.status)
			.build();
		ProjectRemovalJobEntity job = removalRepository.save(projectRemovalJobEntity);
		return job.getId().toString();
	}

	@Override
	public String update(String id, ProjectInstallationStatus status) {
		installationRepository.findById(UUID.fromString(id))
			.map(job -> ProjectInstallationJobEntity.builder()
				.id(job.getId())
				.correlationId(job.correlationId)
				.siteId(job.siteId)
				.projectId(job.projectId)
				.status(status)
				.build())
			.ifPresent(installationRepository::save);
		return id;
	}

	@Override
	public String update(String id, ProjectUpdateStatus status) {
		updateRepository.findById(UUID.fromString(id))
			.map(job -> ProjectUpdateJobEntity.builder()
				.id(job.getId())
				.correlationId(job.correlationId)
				.siteId(job.siteId)
				.projectId(job.projectId)
				.status(status)
				.build())
			.ifPresent(updateRepository::save);
		return id;
	}

	@Override
	public String update(String id, ProjectRemovalStatus status) {
		removalRepository.findById(UUID.fromString(id))
			.map(job -> ProjectRemovalJobEntity.builder()
				.id(job.getId())
				.correlationId(job.correlationId)
				.siteId(job.siteId)
				.projectId(job.projectId)
				.status(status)
				.build())
			.ifPresent(removalRepository::save);
		return id;
	}

	@Override
	public boolean existsByProjectId(String projectId) {
		return installationRepository.existsByProjectId(UUID.fromString(projectId));
	}
}

