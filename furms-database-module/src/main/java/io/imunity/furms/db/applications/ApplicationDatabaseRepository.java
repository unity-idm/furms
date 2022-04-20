/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.db.applications;

import io.imunity.furms.domain.applications.ProjectApplication;
import io.imunity.furms.domain.projects.ProjectId;
import io.imunity.furms.domain.users.FenixUserId;
import io.imunity.furms.spi.applications.ApplicationRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toSet;

@Repository
class ApplicationDatabaseRepository implements ApplicationRepository {

	private final ApplicationEntityRepository repository;

	ApplicationDatabaseRepository(ApplicationEntityRepository repository) {
		this.repository = repository;
	}

	@Override
	public Set<FenixUserId> findAllApplyingUsers(ProjectId projectId) {
		return repository.findAllByProjectId(projectId.id).stream()
			.map(applicationEntity -> applicationEntity.userId)
			.map(FenixUserId::new)
			.collect(toSet());
	}

	@Override
	public Set<ProjectApplication> findAllApplyingUsers(List<ProjectId> projectIds) {
		if(projectIds.isEmpty())
			return Set.of();
		List<UUID> ids = projectIds.stream()
			.map(projectId -> projectId.id)
			.collect(Collectors.toList());
		return repository.findAllByProjectIdIn(ids).stream()
			.map(entity -> new ProjectApplication(entity.projectId.toString(), entity.projectName, new FenixUserId(entity.userId)))
			.collect(toSet());
	}

	@Override
	public Set<String> findAllAppliedProjectsIds(FenixUserId userId) {
		return repository.findAllByUserId(userId.id).stream()
			.map(applicationEntity -> applicationEntity.projectId.toString())
			.collect(toSet());
	}

	@Override
	public void create(ProjectId projectId, FenixUserId userId) {
		repository.save(new ApplicationEntity(null, projectId.id, userId.id));
	}

	@Override
	public void remove(ProjectId projectId, FenixUserId userId) {
		repository.deleteByProjectIdAndUserId(projectId.id, userId.id);
	}

	@Override
	public boolean existsBy(ProjectId projectId, FenixUserId fenixUserId) {
		return repository.existsByProjectIdAndUserId(projectId.id, fenixUserId.id);
	}
}
