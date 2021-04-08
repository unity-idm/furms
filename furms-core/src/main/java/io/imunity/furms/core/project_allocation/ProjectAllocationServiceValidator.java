/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.core.project_allocation;

import io.imunity.furms.api.validation.exceptions.DuplicatedNameValidationError;
import io.imunity.furms.api.validation.exceptions.IdNotFoundValidationError;
import io.imunity.furms.domain.project_allocation.ProjectAllocation;
import io.imunity.furms.spi.community_allocation.CommunityAllocationRepository;
import io.imunity.furms.spi.project_allocation.ProjectAllocationRepository;
import io.imunity.furms.spi.projects.ProjectRepository;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.Optional;

import static io.imunity.furms.core.constant.ValidationConst.MAX_NAME_LENGTH;
import static io.imunity.furms.utils.ValidationUtils.check;
import static org.springframework.util.Assert.notNull;

@Component
class ProjectAllocationServiceValidator {
	private final ProjectAllocationRepository projectAllocationRepository;
	private final CommunityAllocationRepository communityAllocationRepository;
	private final ProjectRepository projectRepository;

	ProjectAllocationServiceValidator(ProjectAllocationRepository projectAllocationRepository,
	                                  CommunityAllocationRepository communityAllocationRepository,
	                                  ProjectRepository projectRepository) {
		this.projectAllocationRepository = projectAllocationRepository;
		this.communityAllocationRepository = communityAllocationRepository;
		this.projectRepository = projectRepository;
	}

	void validateCreate(ProjectAllocation projectAllocation) {
		notNull(projectAllocation, "ProjectAllocation object cannot be null.");
		validateProjectId(projectAllocation.projectId);
		validateCommunityAllocationId(projectAllocation.communityAllocationId);
		validateName(projectAllocation);
		notNull(projectAllocation.amount, "ProjectAllocation amount cannot be null.");
	}

	void validateUpdate(ProjectAllocation projectAllocation) {
		notNull(projectAllocation, "ProjectAllocation object cannot be null.");
		validateId(projectAllocation.id);
		validateName(projectAllocation);
		validateUpdateProjectId(projectAllocation);
		validateUpdateCommunityAllocationId(projectAllocation);
		notNull(projectAllocation.amount, "ProjectAllocation amount cannot be null.");
	}

	void validateDelete(String id) {
		validateId(id);
	}

	private void validateName(ProjectAllocation projectAllocation) {
		notNull(projectAllocation.name, "ProjectAllocation name has to be declared.");
		validateLength("name", projectAllocation.name, MAX_NAME_LENGTH);
		if (isNameUnique(projectAllocation)) {
			throw new DuplicatedNameValidationError("ProjectAllocation name has to be unique.");
		}
	}

	private boolean isNameUnique(ProjectAllocation projectAllocation) {
		Optional<ProjectAllocation> optionalProject = projectAllocationRepository.findById(projectAllocation.id);
		return !projectAllocationRepository.isUniqueName(projectAllocation.name) &&
			(optionalProject.isEmpty() || !optionalProject.get().name.equals(projectAllocation.name));
	}

	private void validateLength(String fieldName, String fieldVale, int length) {
		if (Objects.nonNull(fieldVale) && fieldVale.length() > length) {
			throw new IllegalArgumentException("ProjectAllocation " + fieldName + " is too long.");
		}
	}

	private void validateId(String id) {
		notNull(id, "Resource CreditAllocation ID has to be declared.");
		check(projectAllocationRepository.exists(id), () -> new IdNotFoundValidationError("ProjectAllocation with declared ID is not exists."));
	}

	private void validateProjectId(String id) {
		notNull(id, "Project ID has to be declared.");
		check(projectRepository.exists(id), () -> new IdNotFoundValidationError("Project with declared ID is not exists."));
	}

	private void validateCommunityAllocationId(String id) {
		notNull(id, "CommunityAllocation ID has to be declared.");
		check(communityAllocationRepository.exists(id), () -> new IdNotFoundValidationError("CommunityAllocation with declared ID does not exist."));
	}

	private void validateUpdateProjectId(ProjectAllocation projectAllocation) {
		validateProjectId(projectAllocation.projectId);
		projectAllocationRepository.findById(projectAllocation.id)
			.map(s -> s.projectId)
			.filter(id -> id.equals(projectAllocation.projectId))
			.orElseThrow(() -> new IllegalArgumentException("Project ID change is forbidden"));
	}

	private void validateUpdateCommunityAllocationId(ProjectAllocation projectAllocation) {
		validateCommunityAllocationId(projectAllocation.communityAllocationId);
		communityAllocationRepository.findById(projectAllocation.communityAllocationId)
			.map(s -> s.id)
			.filter(id -> id.equals(projectAllocation.communityAllocationId))
			.orElseThrow(() -> new IllegalArgumentException("Community Allocation ID change is forbidden"));
	}
}
