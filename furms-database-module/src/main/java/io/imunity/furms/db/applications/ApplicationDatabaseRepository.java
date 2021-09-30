/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.db.applications;

import io.imunity.furms.domain.users.FenixUserId;
import io.imunity.furms.spi.applications.ApplicationRepository;
import org.springframework.stereotype.Repository;

import java.util.Set;
import java.util.UUID;

import static java.util.stream.Collectors.toSet;

@Repository
class ApplicationDatabaseRepository implements ApplicationRepository {

	private final ApplicationEntityRepository repository;

	ApplicationDatabaseRepository(ApplicationEntityRepository repository) {
		this.repository = repository;
	}

	@Override
	public Set<FenixUserId> findAllApplyingUsers(String projectId) {
		return repository.findAllByProjectId(UUID.fromString(projectId)).stream()
			.map(applicationEntity -> applicationEntity.userId)
			.map(FenixUserId::new)
			.collect(toSet());
	}

	@Override
	public Set<String> findAllAppliedProjectsIds(FenixUserId userId) {
		return repository.findAllByUserId(userId.id).stream()
			.map(applicationEntity -> applicationEntity.projectId.toString())
			.collect(toSet());
	}

	@Override
	public void create(String projectId, FenixUserId userId) {
		repository.save(new ApplicationEntity(null, UUID.fromString(projectId), userId.id));
	}

	@Override
	public void remove(String projectId, FenixUserId userId) {
		repository.deleteByProjectIdAndUserId(UUID.fromString(projectId), userId.id);
	}

	@Override
	public boolean existsBy(String projectId, FenixUserId fenixUserId) {
		return repository.existsByProjectIdAndUserId(UUID.fromString(projectId), fenixUserId.id);
	}
}
