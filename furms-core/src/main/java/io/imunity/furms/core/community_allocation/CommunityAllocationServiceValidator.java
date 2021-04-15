/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.core.community_allocation;

import io.imunity.furms.api.validation.exceptions.CommunityAllocationHasProjectAllocationsRemoveValidationError;
import io.imunity.furms.api.validation.exceptions.DuplicatedNameValidationError;
import io.imunity.furms.api.validation.exceptions.IdNotFoundValidationError;
import io.imunity.furms.domain.community_allocation.CommunityAllocation;
import io.imunity.furms.spi.communites.CommunityRepository;
import io.imunity.furms.spi.community_allocation.CommunityAllocationRepository;
import io.imunity.furms.spi.project_allocation.ProjectAllocationRepository;
import io.imunity.furms.spi.resource_credits.ResourceCreditRepository;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.Optional;

import static io.imunity.furms.core.constant.ValidationConst.MAX_NAME_LENGTH;
import static io.imunity.furms.utils.ValidationUtils.assertTrue;
import static org.springframework.util.Assert.notNull;

@Component
class CommunityAllocationServiceValidator {
	private final CommunityAllocationRepository communityAllocationRepository;
	private final ProjectAllocationRepository projectAllocationRepository;
	private final ResourceCreditRepository resourceCreditRepository;
	private final CommunityRepository communityRepository;

	CommunityAllocationServiceValidator(CommunityAllocationRepository communityAllocationRepository,
	                                    ProjectAllocationRepository projectAllocationRepository,
	                                    ResourceCreditRepository resourceCreditRepository,
	                                    CommunityRepository communityRepository) {
		this.communityAllocationRepository = communityAllocationRepository;
		this.projectAllocationRepository = projectAllocationRepository;
		this.resourceCreditRepository = resourceCreditRepository;
		this.communityRepository = communityRepository;
	}

	void validateCreate(CommunityAllocation communityAllocation) {
		notNull(communityAllocation, "CommunityAllocation object cannot be null.");
		validateCommunityId(communityAllocation.communityId);
		validateResourceCreditId(communityAllocation.resourceCreditId);
		validateName(communityAllocation);
		notNull(communityAllocation.amount, "CommunityAllocation amount cannot be null.");
	}

	void validateUpdate(CommunityAllocation communityAllocation) {
		notNull(communityAllocation, "CommunityAllocation object cannot be null.");
		validateId(communityAllocation.id);
		validateUpdateCommunityId(communityAllocation);
		validateUpdateResourceCreditId(communityAllocation);
		validateName(communityAllocation);
		notNull(communityAllocation.amount, "CommunityAllocation amount cannot be null.");
	}

	void validateDelete(String id) {
		validateId(id);
		if(projectAllocationRepository.existsByCommunityAllocationId(id)){
			throw new CommunityAllocationHasProjectAllocationsRemoveValidationError("ResourceTypeCredit should not have CommunityAllocations.");
		}
	}

	private void validateName(CommunityAllocation communityAllocation) {
		notNull(communityAllocation.name, "CommunityAllocation name has to be declared.");
		validateLength("name", communityAllocation.name, MAX_NAME_LENGTH);
		if (isNameUnique(communityAllocation)) {
			throw new DuplicatedNameValidationError("CommunityAllocation name has to be unique.");
		}
	}

	private boolean isNameUnique(CommunityAllocation communityAllocation) {
		Optional<CommunityAllocation> optionalProject = communityAllocationRepository.findById(communityAllocation.id);
		return !communityAllocationRepository.isUniqueName(communityAllocation.name) &&
			(optionalProject.isEmpty() || !optionalProject.get().name.equals(communityAllocation.name));
	}

	private void validateLength(String fieldName, String fieldVale, int length) {
		if (Objects.nonNull(fieldVale) && fieldVale.length() > length) {
			throw new IllegalArgumentException("CommunityAllocation " + fieldName + " is too long.");
		}
	}

	private void validateId(String id) {
		notNull(id, "Resource CreditAllocation ID has to be declared.");
		assertTrue(communityAllocationRepository.exists(id), () -> new IdNotFoundValidationError("CommunityAllocation with declared ID is not exists."));
	}

	private void validateCommunityId(String id) {
		notNull(id, "Site ID has to be declared.");
		assertTrue(communityRepository.exists(id), () -> new IdNotFoundValidationError("Community with declared ID is not exists."));
	}

	private void validateResourceCreditId(String id) {
		notNull(id, "ResourceType ID has to be declared.");
		assertTrue(resourceCreditRepository.exists(id), () -> new IdNotFoundValidationError("ResourceCredit with declared ID does not exist."));
	}

	private void validateUpdateCommunityId(CommunityAllocation communityAllocation) {
		validateCommunityId(communityAllocation.communityId);
		communityAllocationRepository.findById(communityAllocation.id)
			.map(s -> s.communityId)
			.filter(id -> id.equals(communityAllocation.communityId))
			.orElseThrow(() -> new IllegalArgumentException("Community ID change is forbidden"));
	}

	private void validateUpdateResourceCreditId(CommunityAllocation communityAllocation) {
		validateResourceCreditId(communityAllocation.resourceCreditId);
		communityAllocationRepository.findById(communityAllocation.id)
			.map(s -> s.resourceCreditId)
			.filter(id -> id.equals(communityAllocation.resourceCreditId))
			.orElseThrow(() -> new IllegalArgumentException("Resource Credit ID change is forbidden"));
	}
}
