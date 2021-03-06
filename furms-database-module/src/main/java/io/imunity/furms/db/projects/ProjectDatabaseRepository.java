/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.db.projects;

import static java.util.Optional.empty;
import static java.util.Optional.ofNullable;
import static java.util.UUID.fromString;
import static java.util.function.Predicate.not;
import static java.util.stream.Collectors.toSet;
import static java.util.stream.StreamSupport.stream;
import static org.springframework.util.StringUtils.isEmpty;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.springframework.stereotype.Repository;

import io.imunity.furms.domain.projects.Project;
import io.imunity.furms.spi.projects.ProjectRepository;

@Repository
class ProjectDatabaseRepository implements ProjectRepository {

	private final ProjectEntityRepository repository;

	ProjectDatabaseRepository(ProjectEntityRepository repository) {
		this.repository = repository;
	}

	@Override
	public Optional<Project> findById(String id) {
		if (isEmpty(id)) {
			return empty();
		}
		return repository.findById(fromString(id))
				.map(ProjectEntity::toProject);
	}

	@Override
	public Set<Project> findAllByCommunityId(String communityId) {
		return repository.findAllByCommunityId(fromString(communityId))
				.map(ProjectEntity::toProject)
				.collect(toSet());
	}

	@Override
	public Set<Project> findAllNotExpiredByCommunityId(String communityId) {
		return repository.findAllByCommunityId(fromString(communityId))
				.map(ProjectEntity::toProject)
				.filter(not(Project::isExpired))
				.collect(toSet());
	}

	@Override
	public boolean isProjectRelatedWithCommunity(String communityId, String projectId) {
		return repository.existsByCommunityIdAndId(fromString(communityId), fromString(projectId));
	}

	@Override
	public Set<Project> findAll() {
		return stream(repository.findAll().spliterator(), false)
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
			.startTime(project.getUtcStartTime())
			.endTime(project.getUtcEndTime())
			.leaderId(ofNullable(project.getLeaderId()).map(leader -> leader.id).orElse(null))
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
					.startTime(project.getUtcStartTime())
					.endTime(project.getUtcEndTime())
					.leaderId(ofNullable(project.getLeaderId()).map(leader -> leader.id).orElse(null))
					.build())
				.map(repository::save)
				.map(ProjectEntity::getId)
				.map(UUID::toString)
				.orElseThrow(() -> new IllegalStateException("Project not found: " + project));
	}

	@Override
	public boolean exists(String id) {
		if (isEmpty(id)) {
			return false;
		}
		return repository.existsById(fromString(id));
	}

	@Override
	public boolean isNamePresent(String communityId, String name) {
		return !repository.existsByCommunityIdAndName(UUID.fromString(communityId), name);
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
