/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.db.project_installation;

import io.imunity.furms.domain.project_installation.ProjectInstallation;
import io.imunity.furms.domain.project_installation.ProjectInstallationJob;
import io.imunity.furms.domain.project_installation.ProjectInstallationStatus;
import io.imunity.furms.domain.site_agent.CorrelationId;
import io.imunity.furms.domain.users.FURMSUser;
import io.imunity.furms.domain.users.PersistentId;
import io.imunity.furms.spi.project_installation.ProjectInstallationRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;

@Repository
class ProjectInstallationJobDatabaseRepository implements ProjectInstallationRepository {
	private final ProjectInstallationJobEntityRepository repository;

	ProjectInstallationJobDatabaseRepository(ProjectInstallationJobEntityRepository repository) {
		this.repository = repository;
	}

	@Override
	public ProjectInstallationJob findByCorrelationId(CorrelationId correlationId) {
		ProjectInstallationJobEntity job = repository.findByCorrelationId(UUID.fromString(correlationId.id));
		return ProjectInstallationJob.builder()
			.id(job.getId().toString())
			.correlationId(new CorrelationId(job.correlationId.toString()))
			.siteId(job.siteId.toString())
			.projectId(job.projectId.toString())
			.status(job.status)
			.build();
	}

	@Override
	public ProjectInstallation findProjectInstallation(String projectAllocationId, Function<PersistentId, Optional<FURMSUser>> userGetter){
		ProjectInstallationEntity allocation = repository.findByProjectAllocationId(UUID.fromString(projectAllocationId));
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
		ProjectInstallationJobEntity job = repository.save(projectInstallationJobEntity);
		return job.getId().toString();
	}

	@Override
	public String update(String id, ProjectInstallationStatus status) {
		repository.findById(UUID.fromString(id))
			.map(job -> ProjectInstallationJobEntity.builder()
				.id(job.getId())
				.correlationId(job.correlationId)
				.siteId(job.siteId)
				.projectId(job.projectId)
				.status(status)
				.build())
			.ifPresent(repository::save);
		return id;
	}

	@Override
	public boolean existsByProjectId(String siteId, String projectId) {
		return repository.existsBySiteIdAndProjectId(UUID.fromString(siteId), UUID.fromString(projectId));
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

