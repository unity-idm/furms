/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.core.resource_credits;

import io.imunity.furms.api.validation.exceptions.CreditUpdateBelowDistributedAmountException;
import io.imunity.furms.api.validation.exceptions.DuplicatedNameValidationError;
import io.imunity.furms.api.validation.exceptions.IdNotFoundValidationError;
import io.imunity.furms.api.validation.exceptions.ResourceCreditHasAllocationException;
import io.imunity.furms.domain.resource_credits.ResourceCredit;
import io.imunity.furms.domain.resource_credits.ResourceCreditId;
import io.imunity.furms.domain.resource_types.ResourceTypeId;
import io.imunity.furms.domain.sites.SiteId;
import io.imunity.furms.spi.community_allocation.CommunityAllocationRepository;
import io.imunity.furms.spi.resource_credits.ResourceCreditRepository;
import io.imunity.furms.spi.resource_type.ResourceTypeRepository;
import io.imunity.furms.spi.sites.SiteRepository;
import io.imunity.furms.utils.ValidationUtils;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Optional;

import static io.imunity.furms.core.constant.ValidationConst.MAX_NAME_LENGTH;
import static io.imunity.furms.utils.ValidationUtils.assertTrue;
import static org.springframework.util.Assert.notNull;

@Component
class ResourceCreditServiceValidator {
	private final CommunityAllocationRepository communityAllocationRepository;
	private final ResourceCreditRepository resourceCreditRepository;
	private final ResourceTypeRepository resourceTypeRepository;
	private final SiteRepository siteRepository;

	ResourceCreditServiceValidator(CommunityAllocationRepository communityAllocationRepository,
	                               ResourceCreditRepository resourceCreditRepository,
	                               ResourceTypeRepository resourceTypeRepository, SiteRepository siteRepository) {
		this.communityAllocationRepository = communityAllocationRepository;
		this.resourceCreditRepository = resourceCreditRepository;
		this.resourceTypeRepository = resourceTypeRepository;
		this.siteRepository = siteRepository;
	}

	void validateCreate(ResourceCredit resourceCredit) {
		notNull(resourceCredit, "ResourceCredit object cannot be null.");
		validateSiteId(resourceCredit.siteId);
		validateResourceTypeId(resourceCredit.resourceTypeId);
		validateName(resourceCredit, Optional.empty());
		notNull(resourceCredit.amount, "ResourceCredit amount cannot be null.");
		validateCreateTime(resourceCredit.utcCreateTime);
		assertAmountIsGreaterThen0(resourceCredit);
		validateTime(resourceCredit.utcStartTime, resourceCredit.utcEndTime);
	}

	void validateUpdate(ResourceCredit resourceCredit) {
		notNull(resourceCredit, "ResourceCredit object cannot be null.");
		ResourceCredit existing = assertCreditWithIdExists(resourceCredit.id);
		validateSplitChange(resourceCredit, existing);
		validateUpdateSiteId(resourceCredit);
		validateUpdateResourceTypeId(resourceCredit);
		validateName(resourceCredit, Optional.of(existing));
		notNull(resourceCredit.amount, "ResourceCredit amount cannot be null.");
		validateCreateTime(resourceCredit.utcCreateTime);
		validateTime(resourceCredit.utcStartTime, resourceCredit.utcEndTime);
		assertStartAndEndTimeIsNotChangeAfterAllocation(resourceCredit);
		assertAmountAboveAlreadyDistributed(resourceCredit, existing);
	}

	private void assertAmountIsGreaterThen0(ResourceCredit resourceCredit) {
		if (resourceCredit.amount.compareTo(BigDecimal.ZERO) < 1)
			throw new IllegalArgumentException("ResourceCredit " + resourceCredit.amount + " have to grater then 0");
	}
	private void assertAmountAboveAlreadyDistributed(ResourceCredit updated, ResourceCredit existing) {
		BigDecimal remaining = communityAllocationRepository.getAvailableAmount(existing.id);
		BigDecimal distributed = existing.amount.subtract(remaining);
		assertTrue(updated.amount.compareTo(distributed) >= 0,
			CreditUpdateBelowDistributedAmountException::new);
	}

	void validateDelete(ResourceCreditId id) {
		assertCreditWithIdExists(id);
		assertCommunityAllocationNotExistsByResourceId(id);
	}

	private void validateName(ResourceCredit resourceCredit, Optional<ResourceCredit> existingCredit) {
		notNull(resourceCredit.name, "ResourceCredit name has to be declared.");
		validateLength("name", resourceCredit.name, MAX_NAME_LENGTH);
		if (isNameOccupied(resourceCredit, existingCredit)) {
			throw new DuplicatedNameValidationError("ResourceCredit name has to be unique.");
		}
	}

	private boolean isNameOccupied(ResourceCredit resourceCredit, Optional<ResourceCredit> existingCredit) {
		if (existingCredit.isPresent() && existingCredit.get().name.equals(resourceCredit.name))
			return false;
		return resourceCreditRepository.isNamePresent(resourceCredit.name, resourceCredit.siteId);
	}

	private void validateLength(String fieldName, String fieldVale, int length) {
		if (Objects.nonNull(fieldVale) && fieldVale.length() > length) {
			throw new IllegalArgumentException("ResourceCredit " + fieldName + " is too long.");
		}
	}

	private ResourceCredit assertCreditWithIdExists(ResourceCreditId id) {
		notNull(id, "Resource Credit ID has to be declared.");
		Optional<ResourceCredit> existing = resourceCreditRepository.findById(id);
		assertTrue(existing.isPresent(), () -> new IdNotFoundValidationError("ResourceCredit with declared ID does not exist."));
		return existing.get();
	}

	private void validateSiteId(SiteId id) {
		notNull(id, "Site ID has to be declared.");
		assertTrue(siteRepository.exists(id), () -> new IdNotFoundValidationError("Site with declared ID is not exists."));
	}

	private void validateResourceTypeId(ResourceTypeId id) {
		notNull(id, "ResourceType ID has to be declared.");
		assertTrue(resourceTypeRepository.exists(id), 
				() -> new IdNotFoundValidationError("ResourceType with declared ID does not exist."));
	}

	private void validateUpdateSiteId(ResourceCredit resourceCredit) {
		validateSiteId(resourceCredit.siteId);
		resourceCreditRepository.findById(resourceCredit.id)
			.map(s -> s.siteId)
			.filter(id -> id.equals(resourceCredit.siteId))
			.orElseThrow(() -> new IllegalArgumentException("Site ID change is forbidden"));
	}

	private void validateUpdateResourceTypeId(ResourceCredit resourceCredit) {
		validateResourceTypeId(resourceCredit.resourceTypeId);
		resourceCreditRepository.findById(resourceCredit.id)
			.map(credit -> credit.resourceTypeId)
			.filter(id -> id.equals(resourceCredit.resourceTypeId))
			.orElseThrow(() -> new IllegalArgumentException("Resource Type ID change is forbidden"));
	}

	private void validateSplitChange(ResourceCredit updated, ResourceCredit existing) {
		if (!updated.splittable && existing.splittable)
			ValidationUtils.assertFalse(communityAllocationRepository.existsByResourceCreditId(existing.id),
					() -> new ResourceCreditHasAllocationException("Can not set credit to non-splittable if it already was allocated"));
	}


	private void validateCreateTime(LocalDateTime createTime) {
		notNull(createTime, "ResourceCredit create time cannot be null");
		if(createTime.isAfter(LocalDateTime.now())){
			throw new IllegalArgumentException("ResourceCredit createTime time must be earlier than now");
		}
	}

	private void validateTime(LocalDateTime startTime, LocalDateTime endTime) {
		notNull(startTime, "ResourceCredit start time cannot be null");
		notNull(endTime, "ResourceCredit end time cannot be null");
		if(startTime.isAfter(endTime)){
			throw new IllegalArgumentException("ResourceCredit start time must be earlier than end time");
		}
	}

	private void assertStartAndEndTimeIsNotChangeAfterAllocation(ResourceCredit resourceCredit) {
		ResourceCredit savedResourceCredit = resourceCreditRepository.findById(resourceCredit.id)
			.orElseThrow(() -> new IllegalArgumentException("ResourceCredit id not found: " + resourceCredit));
		if(!savedResourceCredit.utcStartTime.isEqual(resourceCredit.utcStartTime) && !savedResourceCredit.utcEndTime.isEqual(resourceCredit.utcEndTime)){
			if(communityAllocationRepository.existsByResourceCreditId(resourceCredit.id))
				throw new ResourceCreditHasAllocationException("Can not change validity to/from when it already was allocated");
		}
	}

	private void assertCommunityAllocationNotExistsByResourceId(ResourceCreditId id) {
		if(communityAllocationRepository.existsByResourceCreditId(id)){
			throw new ResourceCreditHasAllocationException("ResourceTypeCredit can not be removed when community allocation exists");
		}
	}
}
