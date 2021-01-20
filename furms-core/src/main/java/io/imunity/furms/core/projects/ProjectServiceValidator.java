/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.core.projects;

import io.imunity.furms.domain.projects.Project;
import io.imunity.furms.spi.communites.CommunityRepository;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Optional;

import static org.springframework.util.Assert.notNull;

@Component
class ProjectServiceValidator {
	private final io.imunity.furms.spi.projects.ProjectRepository projectRepository;
	private final CommunityRepository communityRepository;

	ProjectServiceValidator(io.imunity.furms.spi.projects.ProjectRepository projectRepository, CommunityRepository communityRepository) {
		this.projectRepository = projectRepository;
		this.communityRepository = communityRepository;
	}

	void validateCreate(Project project) {
		notNull(project, "Project object cannot be null.");
		validateCommunityId(project.getCommunityId());
		validateName(project);
		validateLength("description", project.getDescription(), 510);
		validateLength("acronym", project.getAcronym(), 8);
		validateLength("researchField", project.getResearchField(), 255);
		validateTime(project.getStartTime(), project.getEndTime());
	}

	void validateUpdate(Project project) {
		notNull(project, "Project object cannot be null.");
		validateId(project.getId());
		validateUpdateCommunityId(project);
		validateName(project);
		validateLength("description", project.getDescription(), 510);
		validateLength("acronym", project.getAcronym(), 8);
		validateLength("researchField", project.getResearchField(), 255);
		validateTime(project.getStartTime(), project.getEndTime());
	}

	void validateDelete(String id) {
		validateId(id);
	}

	private void validateName(Project project) {
		notNull(project.getName(), "Project name has to be declared.");
		validateLength("name", project.getName(), 255);
		if (isNameUnique(project)) {
			throw new IllegalArgumentException("Project name has to be unique.");
		}
	}

	private void validateLength(String fieldName, String fieldVale, int length) {
		if (Objects.nonNull(fieldVale) && fieldVale.length() > length) {
			throw new IllegalArgumentException("Project " + fieldName + " is too long.");
		}
	}

	private void validateTime(LocalDateTime startTime, LocalDateTime endTime) {
		notNull(startTime, "Project start time cannot be null");
		notNull(endTime, "Project start time cannot be null");
		if(startTime.isAfter(endTime)){
			throw new IllegalArgumentException("Project start time must be earlier than end time");
		}
	}

	private boolean isNameUnique(Project project) {
		Optional<Project> optionalProject = projectRepository.findById(project.getId());
		return !projectRepository.isUniqueName(project.getName()) &&
			(optionalProject.isEmpty() || !optionalProject.get().getName().equals(project.getName()));
	}

	private void validateId(String id) {
		notNull(id, "Project ID has to be declared.");
		if (!projectRepository.exists(id)) {
			throw new IllegalArgumentException("Project with declared ID is not exists.");
		}
	}

	private void validateCommunityId(String id) {
		notNull(id, "Community ID has to be declared.");
		if (!communityRepository.exists(id)) {
			throw new IllegalArgumentException("Community with declared ID is not exists.");
		}
	}

	private void validateUpdateCommunityId(Project project) {
		validateCommunityId(project.getCommunityId());
		projectRepository.findById(project.getId())
			.map(Project::getCommunityId)
			.filter(id -> id.equals(project.getCommunityId()))
			.orElseThrow(() -> new IllegalArgumentException("Community ID change is forbidden"));
	}
}