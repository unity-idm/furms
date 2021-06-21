/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.core.project_allocation;

import io.imunity.furms.api.validation.exceptions.CommunityIsNotRelatedWithCommunityAllocation;
import io.imunity.furms.api.validation.exceptions.DuplicatedNameValidationError;
import io.imunity.furms.api.validation.exceptions.IdNotFoundValidationError;
import io.imunity.furms.api.validation.exceptions.ProjectAllocationIsNotInTerminalStateException;
import io.imunity.furms.api.validation.exceptions.ProjectAllocationWrongAmountException;
import io.imunity.furms.api.validation.exceptions.ProjectExpiredException;
import io.imunity.furms.api.validation.exceptions.ProjectIsNotRelatedWithCommunity;
import io.imunity.furms.api.validation.exceptions.ProjectIsNotRelatedWithProjectAllocation;
import io.imunity.furms.api.validation.exceptions.ResourceCreditExpiredException;
import io.imunity.furms.domain.project_allocation.ProjectAllocation;
import io.imunity.furms.domain.project_allocation_installation.ProjectAllocationInstallation;
import io.imunity.furms.domain.project_allocation_installation.ProjectDeallocation;
import io.imunity.furms.domain.projects.Project;
import io.imunity.furms.domain.resource_usage.ResourceUsage;
import io.imunity.furms.spi.community_allocation.CommunityAllocationRepository;
import io.imunity.furms.spi.project_allocation.ProjectAllocationRepository;
import io.imunity.furms.spi.project_allocation_installation.ProjectAllocationInstallationRepository;
import io.imunity.furms.spi.projects.ProjectRepository;
import io.imunity.furms.spi.resource_usage.ResourceUsageRepository;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.Optional;

import static io.imunity.furms.core.constant.ValidationConst.MAX_NAME_LENGTH;
import static io.imunity.furms.utils.ValidationUtils.assertFalse;
import static io.imunity.furms.utils.ValidationUtils.assertTrue;
import static org.springframework.util.Assert.notNull;

@Component
class ProjectAllocationServiceValidator {
	private final ProjectAllocationRepository projectAllocationRepository;
	private final ProjectAllocationInstallationRepository projectAllocationInstallationRepository;
	private final CommunityAllocationRepository communityAllocationRepository;
	private final ProjectRepository projectRepository;
	private final ResourceUsageRepository resourceUsageRepository;

	ProjectAllocationServiceValidator(ProjectAllocationRepository projectAllocationRepository,
	                                  ProjectAllocationInstallationRepository projectAllocationInstallationRepository,
	                                  CommunityAllocationRepository communityAllocationRepository,
	                                  ProjectRepository projectRepository, ResourceUsageRepository resourceUsageRepository) {
		this.projectAllocationRepository = projectAllocationRepository;
		this.projectAllocationInstallationRepository = projectAllocationInstallationRepository;
		this.communityAllocationRepository = communityAllocationRepository;
		this.projectRepository = projectRepository;
		this.resourceUsageRepository = resourceUsageRepository;
	}

	void validateCreate(String communityId, ProjectAllocation projectAllocation) {
		notNull(projectAllocation, "ProjectAllocation object cannot be null.");
		assertProjectBelongsToCommunity(communityId, projectAllocation.projectId);
		assertProjectExists(projectAllocation.projectId);
		assertProjectNotExpired(projectAllocation.projectId);
		validateCommunityAllocationId(projectAllocation.communityAllocationId);
		assertResourceCreditNotExpired(projectAllocation.communityAllocationId);
		validateName(communityId, projectAllocation);
		assertAmountNotNull(projectAllocation.amount);
	}

	void validateUpdate(String communityId, ProjectAllocation projectAllocation) {
		notNull(projectAllocation, "ProjectAllocation object cannot be null.");

		assertProjectAllocationExists(projectAllocation.id);
		assertProjectBelongsToCommunity(communityId, projectAllocation.projectId);

		validateUpdateProjectId(projectAllocation);
		validateUpdateCommunityAllocationId(projectAllocation);
		validateName(communityId, projectAllocation);

		assertAmountNotNull(projectAllocation.amount);
		assertAmountNotIncreasedInExpiredProject(projectAllocation);
		assertAmountNotIncreasedBeyondCommunityAllocationLimit(projectAllocation);
		assertAmountNotDecreasedBelowConsumedUsage(projectAllocation);
		assertStatusIsInTerminalState(projectAllocation);
	}

	void validateCommunityIdAndProjectId(String communityId, String projectId) {
		assertProjectBelongsToCommunity(communityId, projectId);
	}

	void validateCommunityIdAndProjectAllocationId(String communityId, String projectAllocationId) {
		String projectId = projectAllocationRepository.findById(projectAllocationId).map(allocation -> allocation.projectId).orElse(null);
		assertProjectBelongsToCommunity(communityId, projectId);
	}

	void validateCommunityIdAndCommunityAllocationId(String communityId, String communityAllocationId) {
		String id = communityAllocationRepository.findById(communityAllocationId).map(allocation -> allocation.communityId).orElse(null);
		if(!communityId.equals(id)){
			throw new CommunityIsNotRelatedWithCommunityAllocation("Community "+ communityId +" is not related with community allocation " + communityAllocationId);
		}
	}

	void validateProjectIdAndProjectAllocationId(String projectId, String projectAllocationId) {
		String id = projectAllocationRepository.findById(projectAllocationId).map(allocation -> allocation.projectId).orElse(null);
		if(!projectId.equals(id)){
			throw new ProjectIsNotRelatedWithProjectAllocation("Project "+ projectId +" is not related with project allocation " + projectAllocationId);
		}
	}

	private void assertResourceCreditNotExpired(String communityAllocationId) {
		communityAllocationRepository.findByIdWithRelatedObjects(communityAllocationId)
				.ifPresent(communityAllocation -> assertFalse(communityAllocation.resourceCredit.isExpired(),
						() -> new ResourceCreditExpiredException("Cannot use expired Resource credit")));
	}


	void validateDelete(String communityId, String projectAllocationId) {
		projectAllocationRepository.findById(projectAllocationId)
			.ifPresentOrElse(
				pa -> assertProjectBelongsToCommunity(communityId, pa.projectId),
				() -> {
					throw new IdNotFoundValidationError("ProjectAllocation with declared ID is not exists.");
				}
		);
	}

	private void validateName(String communityId, ProjectAllocation projectAllocation) {
		notNull(projectAllocation.name, "ProjectAllocation name has to be declared.");
		validateLength("name", projectAllocation.name, MAX_NAME_LENGTH);
		if (isNameOccupied(communityId, projectAllocation)) {
			throw new DuplicatedNameValidationError("ProjectAllocation name has to be unique.");
		}
	}

	private void assertProjectBelongsToCommunity(String communityId, String projectId) {
		if (!projectRepository.isProjectRelatedWithCommunity(communityId, projectId)) {
			throw new ProjectIsNotRelatedWithCommunity("Project "+ projectId +" is not related with community " + communityId);
		}
	}

	private boolean isNameOccupied(String communityId, ProjectAllocation projectAllocation) {
		Optional<ProjectAllocation> optionalProject = projectAllocationRepository.findById(projectAllocation.id);
		return !projectAllocationRepository.isNamePresent(communityId, projectAllocation.name) &&
			(optionalProject.isEmpty() || !optionalProject.get().name.equals(projectAllocation.name));
	}

	private void validateLength(String fieldName, String fieldVale, int length) {
		if (Objects.nonNull(fieldVale) && fieldVale.length() > length) {
			throw new IllegalArgumentException("ProjectAllocation " + fieldName + " is too long.");
		}
	}

	private void assertProjectAllocationExists(String id) {
		notNull(id, "Resource CreditAllocation ID has to be declared.");
		assertTrue(projectAllocationRepository.exists(id), () -> new IdNotFoundValidationError("ProjectAllocation with declared ID is not exists."));
	}

	private void assertProjectExists(String id) {
		notNull(id, "Project ID has to be declared.");
		assertTrue(projectRepository.exists(id), () -> new IdNotFoundValidationError("Project with declared ID is not exists."));
	}

	private void assertProjectNotExpired(String projectId) {
		notNull(projectId, "Project ID has to be declared.");
		final Optional<Project> project = projectRepository.findById(projectId);
		assertTrue(project.isPresent() && !project.get().isExpired(),
				() -> new ProjectExpiredException("Project is expired."));
	}

	private void validateCommunityAllocationId(String id) {
		notNull(id, "CommunityAllocation ID has to be declared.");
		assertTrue(communityAllocationRepository.exists(id), () -> new IdNotFoundValidationError("CommunityAllocation with declared ID does not exist."));
	}

	private void validateUpdateProjectId(ProjectAllocation projectAllocation) {
		assertProjectExists(projectAllocation.projectId);
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

	private void assertAmountNotNull(BigDecimal amount) {
		notNull(amount, "ProjectAllocation amount cannot be null.");
	}

	private void assertAmountNotIncreasedInExpiredProject(ProjectAllocation projectAllocation) {
		Optional<Project> project = projectRepository.findById(projectAllocation.projectId);
		if (project.isPresent() && project.get().isExpired()) {
			final Optional<ProjectAllocation> oldAllocation = projectAllocationRepository.findById(projectAllocation.id);
			oldAllocation.ifPresent(old -> assertTrue(
					old.amount.compareTo(projectAllocation.amount) > 0,
					() -> new ProjectAllocationWrongAmountException("Increased allocation amount for expired projects is not permitted.")));
		}
	}

	private void assertAmountNotIncreasedBeyondCommunityAllocationLimit(ProjectAllocation projectAllocation) {
		BigDecimal availableAmount = projectAllocationRepository.getAvailableAmount(projectAllocation.communityAllocationId);
		BigDecimal currentAllocatedAmount = projectAllocationRepository.findById(projectAllocation.id).map(x -> x.amount)
			.orElseThrow(() -> new IllegalArgumentException(String.format("Project Allocation %s doesn't exist", projectAllocation.id)));
		BigDecimal realIncreasedValue = projectAllocation.amount.subtract(currentAllocatedAmount);
		if(availableAmount.subtract(realIncreasedValue).compareTo(BigDecimal.ZERO) <= 0)
			throw new ProjectAllocationWrongAmountException("Allocation amount have to be less then community allocation limit");
	}

	private void assertAmountNotDecreasedBelowConsumedUsage(ProjectAllocation projectAllocation) {
		ResourceUsage resourceUsage = resourceUsageRepository.findCurrentResourceUsage(projectAllocation.id)
			.orElseThrow(() -> new IllegalArgumentException(String.format("Project Allocation %s doesn't exist", projectAllocation.id)));
		if(resourceUsage.cumulativeConsumption.compareTo(projectAllocation.amount) > 0)
			throw new ProjectAllocationWrongAmountException("Allocation amount have to be bigger than consumed usage");
	}

	private void assertStatusIsInTerminalState(ProjectAllocation projectAllocation) {
		ProjectAllocationInstallation projectAllocationInstallation = projectAllocationInstallationRepository.findByProjectAllocationId(projectAllocation.id);
		Optional<ProjectDeallocation> deallocation = projectAllocationInstallationRepository.findDeallocationByProjectAllocationId(projectAllocation.id);
		if(!projectAllocationInstallation.status.isTerminal() || deallocation.isPresent())
			throw new ProjectAllocationIsNotInTerminalStateException(projectAllocation.id);
	}
}
