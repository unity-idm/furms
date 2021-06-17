/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.core.project_allocation;

import io.imunity.furms.api.validation.exceptions.CommunityIsNotRelatedWithCommunityAllocation;
import io.imunity.furms.api.validation.exceptions.DuplicatedNameValidationError;
import io.imunity.furms.api.validation.exceptions.IdNotFoundValidationError;
import io.imunity.furms.api.validation.exceptions.ProjectAllocationWrongAmountException;
import io.imunity.furms.api.validation.exceptions.ProjectExpiredException;
import io.imunity.furms.api.validation.exceptions.ProjectHasMoreThenOneResourceTypeAllocationInGivenTimeException;
import io.imunity.furms.api.validation.exceptions.ProjectIsNotRelatedWithCommunity;
import io.imunity.furms.api.validation.exceptions.ProjectIsNotRelatedWithProjectAllocation;
import io.imunity.furms.api.validation.exceptions.ResourceCreditExpiredException;
import io.imunity.furms.domain.community_allocation.CommunityAllocation;
import io.imunity.furms.domain.project_allocation.ProjectAllocation;
import io.imunity.furms.domain.projects.Project;
import io.imunity.furms.domain.resource_credits.ResourceCredit;
import io.imunity.furms.spi.community_allocation.CommunityAllocationRepository;
import io.imunity.furms.spi.project_allocation.ProjectAllocationRepository;
import io.imunity.furms.spi.projects.ProjectRepository;
import io.imunity.furms.spi.resource_credits.ResourceCreditRepository;
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
	private final ResourceCreditRepository resourceCreditRepository;
	private final ProjectAllocationRepository projectAllocationRepository;
	private final CommunityAllocationRepository communityAllocationRepository;
	private final ProjectRepository projectRepository;

	ProjectAllocationServiceValidator(ResourceCreditRepository resourceCreditRepository,
	                                  ProjectAllocationRepository projectAllocationRepository,
	                                  CommunityAllocationRepository communityAllocationRepository,
	                                  ProjectRepository projectRepository) {
		this.resourceCreditRepository = resourceCreditRepository;
		this.projectAllocationRepository = projectAllocationRepository;
		this.communityAllocationRepository = communityAllocationRepository;
		this.projectRepository = projectRepository;
	}

	void validateCreate(String communityId, ProjectAllocation projectAllocation) {
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

	void validateUpdate(String communityId, ProjectAllocation projectAllocation) {
		notNull(projectAllocation, "ProjectAllocation object cannot be null.");

		assertProjectAllocationExists(projectAllocation.id);
		assertProjectBelongsToCommunity(communityId, projectAllocation.projectId);

		validateUpdateProjectId(projectAllocation);
		validateUpdateCommunityAllocationId(projectAllocation);
		validateName(communityId, projectAllocation);

		assertAmountNotNull(projectAllocation.amount);
		assertAmountNotIncreased(projectAllocation);
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

	private void assertProjectHasUniqueResourceTypeInGivenPointInTime(ProjectAllocation projectAllocation) {
		CommunityAllocation communityAllocation = communityAllocationRepository.findById(projectAllocation.communityAllocationId)
			.orElseThrow(() -> new IllegalStateException(String.format("Community Allocation %s doesn't exist", projectAllocation.communityAllocationId)));
		ResourceCredit resourceCredit = resourceCreditRepository.findById(communityAllocation.resourceCreditId)
			.orElseThrow(() -> new IllegalStateException(String.format("Resource Credit %s doesn't exist", communityAllocation.resourceCreditId)));

		AllocationTimestamp allocationTimestamp = new AllocationTimestamp(resourceCredit.utcStartTime, resourceCredit.utcEndTime);
		boolean matches = projectAllocationRepository.findAllWithRelatedObjects(projectAllocation.projectId).stream()
			.filter(x -> x.resourceType.id.equals(resourceCredit.resourceTypeId))
			.map(x -> new AllocationTimestamp(x.resourceCredit.utcStartTime, x.resourceCredit.utcEndTime))
			.anyMatch(allocationTimestamp::overlaps);

		assertFalse(matches, () -> new ProjectHasMoreThenOneResourceTypeAllocationInGivenTimeException(projectAllocation.projectId, resourceCredit.resourceTypeId));
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

	private void assertAmountNotIncreased(ProjectAllocation projectAllocation) {
		Optional<Project> project = projectRepository.findById(projectAllocation.projectId);
		if (project.isPresent() && project.get().isExpired()) {
			final Optional<ProjectAllocation> oldAllocation = projectAllocationRepository.findById(projectAllocation.id);
			oldAllocation.ifPresent(old -> assertTrue(
					old.amount.compareTo(projectAllocation.amount) > 0,
					() -> new ProjectAllocationWrongAmountException("Increased allocation amount for expired projects is not permitted.")));
		}
	}
}
