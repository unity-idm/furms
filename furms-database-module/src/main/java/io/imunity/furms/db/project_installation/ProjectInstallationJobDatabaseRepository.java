/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.db.project_installation;

import io.imunity.furms.domain.project_installation.ProjectInstallation;
import io.imunity.furms.domain.project_installation.ProjectInstallationJob;
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
		return new ProjectInstallationJob(job.getId().toString(), new CorrelationId(job.correlationId.toString()), job.status);
	}

	@Override
	public ProjectInstallation findProjectInstallation(String projectAllocationId, Function<PersistentId, Optional<FURMSUser>> userGetter){
		ProjectInstallationEntity allocation = repository.findByProjectAllocationId(UUID.fromString(projectAllocationId));
		return ProjectInstallation.builder()
			.id(allocation.id)
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
		ProjectInstallationJobEntity projectInstallationJobEntity = new ProjectInstallationJobEntity(null, projectInstallationJob.status, UUID.fromString(projectInstallationJob.correlationId.id));
		ProjectInstallationJobEntity job = repository.save(projectInstallationJobEntity);
		return job.getId().toString();
	}

	@Override
	public String update(ProjectInstallationJob projectInstallationJob) {
		repository.findById(UUID.fromString(projectInstallationJob.id))
			.map(x -> new ProjectInstallationJobEntity(x.getId(), projectInstallationJob.status, x.correlationId))
			.ifPresent(repository::save);
		return projectInstallationJob.id;
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

