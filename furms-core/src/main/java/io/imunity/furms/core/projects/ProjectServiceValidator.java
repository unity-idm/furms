/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.core.projects;

import io.imunity.furms.api.validation.exceptions.DuplicatedNameValidationError;
import io.imunity.furms.api.validation.exceptions.IdNotFoundValidationError;
import io.imunity.furms.domain.communities.CommunityId;
import io.imunity.furms.domain.projects.ProjectAdminControlledAttributes;
import io.imunity.furms.domain.projects.Project;
import io.imunity.furms.domain.projects.ProjectId;
import io.imunity.furms.spi.communites.CommunityRepository;
import io.imunity.furms.spi.projects.ProjectRepository;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Optional;

import static io.imunity.furms.api.constant.ValidationConst.MAX_RESEARCH_NAME_LENGTH;
import static io.imunity.furms.api.constant.ValidationConst.MAX_ACRONYM_LENGTH;
import static io.imunity.furms.api.constant.ValidationConst.MAX_DESCRIPTION_LENGTH;
import static io.imunity.furms.api.constant.ValidationConst.MAX_PROJECT_NAME_LENGTH;
import static io.imunity.furms.utils.ValidationUtils.assertTrue;
import static org.springframework.util.Assert.notNull;

@Component
class ProjectServiceValidator {
	private final ProjectRepository projectRepository;
	private final CommunityRepository communityRepository;

	ProjectServiceValidator(ProjectRepository projectRepository, CommunityRepository communityRepository) {
		this.projectRepository = projectRepository;
		this.communityRepository = communityRepository;
	}

	void validateCreate(Project project) {
		notNull(project, "Project object cannot be null.");
		validateCommunityId(project.getCommunityId());
		validateName(project);
		validateLength("description", project.getDescription(), MAX_DESCRIPTION_LENGTH);
		notNull(project.getAcronym(), "Acronym cannot be null.");
		validateLength("acronym", project.getAcronym(), MAX_ACRONYM_LENGTH);
		notNull(project.getResearchField(), "Research field cannot be null.");
		validateLength("researchField", project.getResearchField(), MAX_RESEARCH_NAME_LENGTH);
		validateTime(project.getUtcStartTime(), project.getUtcEndTime());
	}

	void validateUpdate(Project project) {
		notNull(project, "Project object cannot be null.");
		validateId(project.getId());
		validateUpdateCommunityId(project);
		validateName(project);
		validateLength("description", project.getDescription(), MAX_DESCRIPTION_LENGTH);
		notNull(project.getAcronym(), "Acronym cannot be null.");
		validateLength("acronym", project.getAcronym(), MAX_ACRONYM_LENGTH);
		notNull(project.getResearchField(), "Research field cannot be null.");
		validateLength("researchField", project.getResearchField(), MAX_RESEARCH_NAME_LENGTH);
		validateTime(project.getUtcStartTime(), project.getUtcEndTime());
	}

	void validateLimitedUpdate(ProjectAdminControlledAttributes project) {
		notNull(project, "Project object cannot be null.");
		validateId(project.getId());
		validateLength("description", project.getDescription(), MAX_DESCRIPTION_LENGTH);
	}

	void validateDelete(ProjectId id) {
		validateId(id);
	}

	private void validateName(Project project) {
		notNull(project.getName(), "Project name has to be declared.");
		validateLength("name", project.getName(), MAX_PROJECT_NAME_LENGTH);
		if (isNameOccupied(project)) {
			throw new DuplicatedNameValidationError("Project name has to be unique.");
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

	private boolean isNameOccupied(Project project) {
		Optional<Project> optionalProject = projectRepository.findById(project.getId());
		return !projectRepository.isNamePresent(project.getCommunityId(), project.getName()) &&
			(optionalProject.isEmpty() || !optionalProject.get().getName().equals(project.getName()));
	}

	private void validateId(ProjectId id) {
		notNull(id, "Project ID has to be declared.");
		assertTrue(projectRepository.exists(id), () -> new IdNotFoundValidationError("Project with declared ID is not exists."));
	}

	private void validateCommunityId(CommunityId id) {
		notNull(id, "Community ID has to be declared.");
		assertTrue(communityRepository.exists(id), () -> new IdNotFoundValidationError("Community with declared ID is not exists."));
	}

	private void validateUpdateCommunityId(Project project) {
		validateCommunityId(project.getCommunityId());
		projectRepository.findById(project.getId())
			.map(Project::getCommunityId)
			.filter(id -> id.equals(project.getCommunityId()))
			.orElseThrow(() -> new IllegalArgumentException("Community ID change is forbidden"));
	}
}
