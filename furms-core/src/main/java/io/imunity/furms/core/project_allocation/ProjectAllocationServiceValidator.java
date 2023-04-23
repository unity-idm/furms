/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.core.project_allocation;

import io.imunity.furms.api.validation.exceptions.CommunityIsNotRelatedWithCommunityAllocation;
import io.imunity.furms.api.validation.exceptions.DuplicatedNameValidationError;
import io.imunity.furms.api.validation.exceptions.IdNotFoundValidationError;
import io.imunity.furms.api.validation.exceptions.ProjectAllocationDecreaseBeyondUsageException;
import io.imunity.furms.api.validation.exceptions.ProjectAllocationIncreaseInExpiredProjectException;
import io.imunity.furms.api.validation.exceptions.ProjectAllocationIsNotInTerminalStateException;
import io.imunity.furms.api.validation.exceptions.ProjectAllocationWrongAmountException;
import io.imunity.furms.api.validation.exceptions.ProjectExpiredException;
import io.imunity.furms.api.validation.exceptions.ProjectHasMoreThenOneResourceTypeAllocationInGivenTimeException;
import io.imunity.furms.api.validation.exceptions.ProjectIsNotRelatedWithCommunity;
import io.imunity.furms.api.validation.exceptions.ProjectIsNotRelatedWithProjectAllocation;
import io.imunity.furms.api.validation.exceptions.ProjectNotInTerminalStateException;
import io.imunity.furms.api.validation.exceptions.ResourceCreditExpiredException;
import io.imunity.furms.core.project_installation.ProjectInstallationService;
import io.imunity.furms.domain.communities.CommunityId;
import io.imunity.furms.domain.community_allocation.CommunityAllocation;
import io.imunity.furms.domain.community_allocation.CommunityAllocationId;
import io.imunity.furms.domain.project_allocation.ProjectAllocation;
import io.imunity.furms.domain.project_allocation.ProjectAllocationId;
import io.imunity.furms.domain.project_allocation_installation.ProjectAllocationInstallation;
import io.imunity.furms.domain.project_allocation_installation.ProjectDeallocation;
import io.imunity.furms.domain.projects.Project;
import io.imunity.furms.domain.projects.ProjectId;
import io.imunity.furms.domain.resource_credits.ResourceCredit;
import io.imunity.furms.spi.community_allocation.CommunityAllocationRepository;
import io.imunity.furms.spi.project_allocation.ProjectAllocationRepository;
import io.imunity.furms.spi.project_allocation_installation.ProjectAllocationInstallationRepository;
import io.imunity.furms.spi.projects.ProjectRepository;
import io.imunity.furms.spi.resource_credits.ResourceCreditRepository;
import io.imunity.furms.spi.resource_usage.ResourceUsageRepository;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.Optional;

import static io.imunity.furms.api.constant.ValidationConst.MAX_ALLOCATION_NAME_LENGTH;
import static io.imunity.furms.utils.ValidationUtils.assertFalse;
import static io.imunity.furms.utils.ValidationUtils.assertTrue;
import static org.springframework.util.Assert.notNull;

@Component
class ProjectAllocationServiceValidator {
	private final ResourceCreditRepository resourceCreditRepository;
	private final ProjectAllocationRepository projectAllocationRepository;
	private final ProjectAllocationInstallationRepository projectAllocationInstallationRepository;
	private final CommunityAllocationRepository communityAllocationRepository;
	private final ProjectRepository projectRepository;
	private final ResourceUsageRepository resourceUsageRepository;
	private final ProjectInstallationService projectInstallationService;

	ProjectAllocationServiceValidator(ResourceCreditRepository resourceCreditRepository,
	                                  ProjectAllocationRepository projectAllocationRepository,
	                                  ProjectAllocationInstallationRepository projectAllocationInstallationRepository,
	                                  CommunityAllocationRepository communityAllocationRepository,
	                                  ProjectRepository projectRepository,
	                                  ResourceUsageRepository resourceUsageRepository,
									  ProjectInstallationService projectInstallationService) {
		this.resourceCreditRepository = resourceCreditRepository;
		this.projectAllocationRepository = projectAllocationRepository;
		this.projectAllocationInstallationRepository = projectAllocationInstallationRepository;
		this.communityAllocationRepository = communityAllocationRepository;
		this.projectRepository = projectRepository;
		this.resourceUsageRepository = resourceUsageRepository;
		this.projectInstallationService = projectInstallationService;
	}

	void validateCreate(CommunityId communityId, ProjectAllocation projectAllocation) {
		notNull(projectAllocation, "ProjectAllocation object cannot be null.");
		assertProjectBelongsToCommunity(communityId, projectAllocation.projectId);
		assertProjectExists(projectAllocation.projectId);
		assertProjectNotExpired(projectAllocation.projectId);
		assertProjectHasUniqueResourceTypeInGivenPointInTime(projectAllocation);
		validateCommunityAllocationId(projectAllocation.communityAllocationId);
		assertResourceCreditNotExpired(projectAllocation.communityAllocationId);
		validateName(communityId, projectAllocation);
		assertAmountNotNull(projectAllocation.amount);
	}

	void validateUpdate(CommunityId communityId, ProjectAllocation projectAllocation) {
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

	void validateCommunityIdAndProjectId(CommunityId communityId, ProjectId projectId) {
		assertProjectBelongsToCommunity(communityId, projectId);
	}

	void validateCommunityIdAndProjectAllocationId(CommunityId communityId, ProjectAllocationId projectAllocationId) {
		ProjectId projectId = projectAllocationRepository.findById(projectAllocationId)
			.map(allocation -> allocation.projectId)
			.orElse(null);
		assertProjectBelongsToCommunity(communityId, projectId);
	}

	void validateCommunityIdAndCommunityAllocationId(CommunityId communityId, CommunityAllocationId communityAllocationId) {
		CommunityId id = communityAllocationRepository.findById(communityAllocationId)
			.map(allocation -> allocation.communityId)
			.orElse(null);
		if(!communityId.equals(id)){
			throw new CommunityIsNotRelatedWithCommunityAllocation("Community "+ communityId +" is not related with community allocation " + communityAllocationId);
		}
	}

	void validateProjectIdAndProjectAllocationId(ProjectId projectId, ProjectAllocationId projectAllocationId) {
		ProjectId id = projectAllocationRepository.findById(projectAllocationId)
			.map(allocation -> allocation.projectId)
			.orElse(null);
		if(!projectId.equals(id)){
			throw new ProjectIsNotRelatedWithProjectAllocation("Project "+ projectId +" is not related with project allocation " + projectAllocationId);
		}
	}

	private void assertResourceCreditNotExpired(CommunityAllocationId communityAllocationId) {
		communityAllocationRepository.findByIdWithRelatedObjects(communityAllocationId)
				.ifPresent(communityAllocation -> assertFalse(communityAllocation.resourceCredit.isExpired(),
						() -> new ResourceCreditExpiredException("Cannot use expired Resource credit")));
	}


	void validateDelete(CommunityId communityId, ProjectAllocationId projectAllocationId) {
		projectAllocationRepository.findById(projectAllocationId)
			.ifPresentOrElse(projectAllocation -> {
					assertProjectBelongsToCommunity(communityId, projectAllocation.projectId);
					assertProjectNotExpired(projectAllocation.projectId);
					assertProjectNotInTerminalState(projectAllocation.projectId);
				},
				() -> {
					throw new IdNotFoundValidationError("ProjectAllocation with declared ID is not exists.");
				}
		);
	}

	private void validateName(CommunityId communityId, ProjectAllocation projectAllocation) {
		notNull(projectAllocation.name, "ProjectAllocation name has to be declared.");
		validateLength("name", projectAllocation.name, MAX_ALLOCATION_NAME_LENGTH);
		if (isNameOccupied(communityId, projectAllocation)) {
			throw new DuplicatedNameValidationError("ProjectAllocation name has to be unique.");
		}
	}

	private void assertProjectBelongsToCommunity(CommunityId communityId, ProjectId projectId) {
		if (!projectRepository.isProjectRelatedWithCommunity(communityId, projectId)) {
			throw new ProjectIsNotRelatedWithCommunity("Project "+ projectId +" is not related with community " + communityId);
		}
	}

	private boolean isNameOccupied(CommunityId communityId, ProjectAllocation projectAllocation) {
		Optional<ProjectAllocation> optionalProject = projectAllocationRepository.findById(projectAllocation.id);
		return !projectAllocationRepository.isNamePresent(communityId, projectAllocation.name) &&
			(optionalProject.isEmpty() || !optionalProject.get().name.equals(projectAllocation.name));
	}

	private void validateLength(String fieldName, String fieldVale, int length) {
		if (Objects.nonNull(fieldVale) && fieldVale.length() > length) {
			throw new IllegalArgumentException("ProjectAllocation " + fieldName + " is too long.");
		}
	}

	private void assertProjectAllocationExists(ProjectAllocationId id) {
		notNull(id, "Resource CreditAllocation ID has to be declared.");
		assertTrue(projectAllocationRepository.exists(id), () -> new IdNotFoundValidationError("ProjectAllocation with declared ID is not exists."));
	}

	private void assertProjectExists(ProjectId id) {
		notNull(id, "Project ID has to be declared.");
		assertTrue(projectRepository.exists(id), () -> new IdNotFoundValidationError("Project with declared ID is not exists."));
	}

	private void assertProjectNotExpired(ProjectId projectId) {
		notNull(projectId, "Project ID has to be declared.");
		final Optional<Project> project = projectRepository.findById(projectId);
		assertTrue(project.isPresent() && !project.get().isExpired(),
				() -> new ProjectExpiredException("Project is expired."));
	}

	private void assertProjectNotInTerminalState(ProjectId projectId) {
		assertTrue(projectInstallationService.isProjectInTerminalState(projectId),
				() -> new ProjectNotInTerminalStateException("Deleted project has to be in terminal state."));
	}

	private void assertProjectHasUniqueResourceTypeInGivenPointInTime(ProjectAllocation projectAllocation) {
		CommunityAllocation communityAllocation = communityAllocationRepository.findById(projectAllocation.communityAllocationId)
			.orElseThrow(() -> new IllegalStateException(String.format("Community Allocation %s doesn't exist", projectAllocation.communityAllocationId)));
		ResourceCredit resourceCredit = resourceCreditRepository.findById(communityAllocation.resourceCreditId)
			.orElseThrow(() -> new IllegalStateException(String.format("Resource Credit %s doesn't exist", communityAllocation.resourceCreditId)));

		AllocationTimespan allocationTimespan = new AllocationTimespan(resourceCredit.utcStartTime, resourceCredit.utcEndTime);
		boolean matches = projectAllocationRepository.findAllWithRelatedObjects(projectAllocation.projectId).stream()
			.filter(x -> x.resourceType.id.equals(resourceCredit.resourceTypeId))
			.map(x -> new AllocationTimespan(x.resourceCredit.utcStartTime, x.resourceCredit.utcEndTime))
			.anyMatch(allocationTimespan::overlaps);

		assertFalse(matches, () -> new ProjectHasMoreThenOneResourceTypeAllocationInGivenTimeException(projectAllocation.projectId, resourceCredit.resourceTypeId));
	}

	private void validateCommunityAllocationId(CommunityAllocationId id) {
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
				ProjectAllocationIncreaseInExpiredProjectException::new));
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
		BigDecimal resourceUsage = resourceUsageRepository.findCurrentResourceUsage(projectAllocation.id)
			.map(usage -> usage.cumulativeConsumption)
			.orElse(BigDecimal.ZERO);
		if(resourceUsage.compareTo(projectAllocation.amount) > 0)
			throw new ProjectAllocationDecreaseBeyondUsageException();
	}

	private void assertStatusIsInTerminalState(ProjectAllocation projectAllocation) {
		ProjectAllocationInstallation projectAllocationInstallation = projectAllocationInstallationRepository.findByProjectAllocationId(projectAllocation.id);
		Optional<ProjectDeallocation> deallocation = projectAllocationInstallationRepository.findDeallocationByProjectAllocationId(projectAllocation.id);
		if(!projectAllocationInstallation.status.isTerminal() || deallocation.isPresent())
			throw new ProjectAllocationIsNotInTerminalStateException(projectAllocation.id);
	}
}
