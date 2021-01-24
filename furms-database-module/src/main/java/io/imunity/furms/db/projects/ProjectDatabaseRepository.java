/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.db.projects;

import io.imunity.furms.domain.projects.Project;
import io.imunity.furms.spi.projects.ProjectRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static java.util.UUID.fromString;
import static java.util.stream.Collectors.toSet;
import static org.springframework.util.StringUtils.isEmpty;

@Repository
class ProjectDatabaseRepository implements ProjectRepository {

	private final ProjectEntityRepository repository;

	ProjectDatabaseRepository(ProjectEntityRepository repository) {
		this.repository = repository;
	}

	@Override
	public Optional<Project> findById(String id) {
		if (isEmpty(id)) {
			return Optional.empty();
		}
		return repository.findById(fromString(id))
				.map(ProjectEntity::toProject);
	}

	@Override
	public Set<Project> findAll(String communityId) {
		return repository.findAllByCommunityId(fromString(communityId))
				.map(ProjectEntity::toProject)
				.collect(toSet());
	}

	@Override
	public String create(Project project) {
		ProjectEntity saved = repository.save(ProjectEntity.builder()
			.communityId(UUID.fromString(project.getCommunityId()))
			.name(project.getName())
			.description(project.getDescription())
			.logo(project.getLogo())
			.acronym(project.getAcronym())
			.researchField(project.getResearchField())
			.startTime(project.getStartTime())
			.endTime(project.getEndTime())
			.build());
		return saved.getId().toString();
	}

	@Override
	public String update(Project project) {
		return repository.findById(UUID.fromString(project.getId()))
				.map(oldEntity -> ProjectEntity.builder()
					.id(UUID.fromString(project.getId()))
					.communityId(UUID.fromString(project.getCommunityId()))
					.name(project.getName())
					.description(project.getDescription())
					.logo(project.getLogo())
					.acronym(project.getAcronym())
					.researchField(project.getResearchField())
					.startTime(project.getStartTime())
					.endTime(project.getEndTime())
					.build())
				.map(repository::save)
				.map(ProjectEntity::getId)
				.map(UUID::toString)
				.get();
	}

	@Override
	public boolean exists(String id) {
		if (isEmpty(id)) {
			return false;
		}
		return repository.existsById(fromString(id));
	}

	@Override
	public boolean isUniqueName(String name) {
		return !repository.existsByName(name);
	}

	@Override
	public void delete(String id) {
		if (isEmpty(id)) {
			throw new IllegalArgumentException("Incorrect delete Project input.");
		}
		repository.deleteById(fromString(id));
	}
	
	@Override
	public void deleteAll() {
		repository.deleteAll();
	}
}
