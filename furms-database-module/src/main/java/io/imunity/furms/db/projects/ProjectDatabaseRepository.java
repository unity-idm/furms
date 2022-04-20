/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.db.projects;

import io.imunity.furms.domain.communities.CommunityId;
import io.imunity.furms.domain.projects.Project;
import io.imunity.furms.domain.projects.ProjectId;
import io.imunity.furms.spi.projects.ProjectRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static java.util.Optional.empty;
import static java.util.Optional.ofNullable;
import static java.util.function.Predicate.not;
import static java.util.stream.Collectors.toSet;
import static java.util.stream.StreamSupport.stream;
import static org.springframework.util.ObjectUtils.isEmpty;

@Repository
class ProjectDatabaseRepository implements ProjectRepository {

	private final ProjectEntityRepository repository;

	ProjectDatabaseRepository(ProjectEntityRepository repository) {
		this.repository = repository;
	}

	@Override
	public Optional<Project> findById(ProjectId id) {
		if (isEmpty(id)) {
			return empty();
		}
		return repository.findById(id.id)
				.map(ProjectEntity::toProject);
	}

	@Override
	public Set<Project> findAllByCommunityId(CommunityId communityId) {
		return repository.findAllByCommunityId(communityId.id).stream()
				.map(ProjectEntity::toProject)
				.collect(toSet());
	}

	@Override
	public Set<Project> findAllByCommunityIds(Set<CommunityId> communityIds) {
		return stream(repository.findAll().spliterator(), false)
			.filter(x -> communityIds.contains(x.getCommunityId().toString()))
			.map(ProjectEntity::toProject)
			.collect(toSet());
	}

	@Override
	public Set<Project> findAllNotExpiredByCommunityId(CommunityId communityId) {
		return repository.findAllByCommunityId(communityId.id).stream()
				.map(ProjectEntity::toProject)
				.filter(not(Project::isExpired))
				.collect(toSet());
	}

	@Override
	public boolean isProjectRelatedWithCommunity(CommunityId communityId, ProjectId projectId) {
		return repository.existsByCommunityIdAndId(communityId.id, projectId.id);
	}

	@Override
	public Set<Project> findAll() {
		return stream(repository.findAll().spliterator(), false)
			.map(ProjectEntity::toProject)
			.collect(toSet());
	}

	@Override
	public Set<Project> findAll(Set<ProjectId> ids) {
		return repository.findAllByIdIn(ids.stream().map(projectId -> projectId.id).collect(toSet())).stream()
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
	public void update(Project project) {
		repository.findById(UUID.fromString(project.getId()))
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
	public boolean exists(ProjectId id) {
		if (isEmpty(id)) {
			return false;
		}
		return repository.existsById(id.id);
	}

	@Override
	public boolean isNamePresent(CommunityId communityId, String name) {
		return !repository.existsByCommunityIdAndName(communityId.id, name);
	}

	@Override
	public void delete(ProjectId id) {
		if (isEmpty(id)) {
			throw new IllegalArgumentException("Incorrect delete Project input.");
		}
		repository.deleteById(id.id);
	}
	
	@Override
	public void deleteAll() {
		repository.deleteAll();
	}
}
