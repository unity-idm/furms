/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.core.community_allocation;

import static io.imunity.furms.core.constant.ValidationConst.MAX_NAME_LENGTH;
import static io.imunity.furms.utils.ValidationUtils.assertFalse;
import static io.imunity.furms.utils.ValidationUtils.assertTrue;
import static org.springframework.util.Assert.notNull;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.Optional;

import org.springframework.stereotype.Component;

import io.imunity.furms.api.validation.exceptions.CommunityAllocationHasProjectAllocationsRemoveValidationError;
import io.imunity.furms.api.validation.exceptions.CommunityAllocationUpdateAboveCreditAmountException;
import io.imunity.furms.api.validation.exceptions.CommunityAllocationUpdateAboveCreditAvailableAmountException;
import io.imunity.furms.api.validation.exceptions.CommunityAllocationUpdateBelowDistributedAmountException;
import io.imunity.furms.api.validation.exceptions.DuplicatedNameValidationError;
import io.imunity.furms.api.validation.exceptions.IdNotFoundValidationError;
import io.imunity.furms.domain.community_allocation.CommunityAllocation;
import io.imunity.furms.domain.resource_credits.ResourceCredit;
import io.imunity.furms.spi.communites.CommunityRepository;
import io.imunity.furms.spi.community_allocation.CommunityAllocationRepository;
import io.imunity.furms.spi.project_allocation.ProjectAllocationRepository;
import io.imunity.furms.spi.resource_credits.ResourceCreditRepository;

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
		assertCommunityExists(communityAllocation.communityId);
		assertResourceCreditExists(communityAllocation.resourceCreditId);
		assertResourceCreditNotExpired(communityAllocation.resourceCreditId);
		validateName(communityAllocation);
		notNull(communityAllocation.amount, "CommunityAllocation amount cannot be null.");
	}

	void validateUpdate(CommunityAllocation updatedAllocation) {
		notNull(updatedAllocation, "CommunityAllocation object cannot be null.");
		CommunityAllocation existingAllocation = assertAllocationExists(updatedAllocation.id);
		assertCommunityIsNotChanged(updatedAllocation, existingAllocation);
		assertResourceCreditIsNotChanged(updatedAllocation, existingAllocation);
		assertNotUpdatedBelowDistributed(updatedAllocation, existingAllocation);
		assertNotUpdatedAboveCredit(updatedAllocation, existingAllocation);
		assertNotUpdatedAboveCreditAvailableAmount(updatedAllocation, existingAllocation);
		validateName(updatedAllocation);
		notNull(updatedAllocation.amount, "CommunityAllocation amount cannot be null.");
	}

	private void assertNotUpdatedAboveCreditAvailableAmount(CommunityAllocation updatedAllocation,
			CommunityAllocation existingAllocation) {
		BigDecimal remainingAmount = communityAllocationRepository.getAvailableAmount(updatedAllocation.resourceCreditId);
		BigDecimal maxAllowedAmount = remainingAmount.add(existingAllocation.amount);
		assertTrue(updatedAllocation.amount.compareTo(maxAllowedAmount) <= 0,
				CommunityAllocationUpdateAboveCreditAvailableAmountException::new);
	}

	private void assertNotUpdatedAboveCredit(CommunityAllocation updatedAllocation,
			CommunityAllocation existingAllocation) {
		ResourceCredit credit = resourceCreditRepository.findById(updatedAllocation.resourceCreditId).get();
		assertFalse(updatedAllocation.amount.compareTo(credit.amount) > 0,
				CommunityAllocationUpdateAboveCreditAmountException::new);
	}

	private void assertNotUpdatedBelowDistributed(CommunityAllocation updatedAllocation, CommunityAllocation existingAllocation) {
		BigDecimal availableAmount = projectAllocationRepository.getAvailableAmount(updatedAllocation.id);
		BigDecimal distributedAmount = existingAllocation.amount.subtract(availableAmount);
		assertFalse(distributedAmount.compareTo(updatedAllocation.amount) > 0,
				CommunityAllocationUpdateBelowDistributedAmountException::new);
	}

	void validateDelete(String id) {
		assertAllocationExists(id);
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
		Optional<CommunityAllocation> optionalAllocation = communityAllocationRepository.findById(communityAllocation.id);
		return !communityAllocationRepository.isUniqueName(communityAllocation.name) &&
			(optionalAllocation.isEmpty() || !optionalAllocation.get().name.equals(communityAllocation.name));
	}

	private void validateLength(String fieldName, String fieldVale, int length) {
		if (Objects.nonNull(fieldVale) && fieldVale.length() > length) {
			throw new IllegalArgumentException("CommunityAllocation " + fieldName + " is too long.");
		}
	}

	private CommunityAllocation assertAllocationExists(String id) {
		notNull(id, "Resource CreditAllocation ID has to be declared.");
		Optional<CommunityAllocation> existing = communityAllocationRepository.findById(id);
		assertTrue(existing.isPresent(), () -> new IdNotFoundValidationError("CommunityAllocation with declared ID does not exist"));
		return existing.get();
	}

	private void assertCommunityExists(String id) {
		notNull(id, "Community ID has to be declared.");
		assertTrue(communityRepository.exists(id), () -> new IdNotFoundValidationError("Community with declared ID does not exist"));
	}

	private void assertResourceCreditExists(String id) {
		notNull(id, "ResourceType ID has to be declared.");
		assertTrue(resourceCreditRepository.exists(id), () -> new IdNotFoundValidationError("ResourceCredit with declared ID does not exist"));
	}

	private void assertResourceCreditNotExpired(String resourceCreditId) {
		resourceCreditRepository.findById(resourceCreditId)
				.ifPresent(credit -> assertFalse(credit.isExpired(), () -> new IllegalArgumentException("Cannot use expired Resource credit")));
	}

	private void assertCommunityIsNotChanged(CommunityAllocation updatedCommunityAllocation, 
			CommunityAllocation existing) {
		assertTrue(existing.communityId.equals(updatedCommunityAllocation.communityId), 
				() -> new IllegalArgumentException("Community ID change is forbidden"));
	}

	private void assertResourceCreditIsNotChanged(CommunityAllocation updatedCommunityAllocation, 
			CommunityAllocation existing) {
		assertTrue(existing.resourceCreditId.equals(updatedCommunityAllocation.resourceCreditId), 
				() -> new IllegalArgumentException("Resource Credit ID change is forbidden"));
	}
}
