/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.core.resource_credit_allocation;

import io.imunity.furms.api.validation.exceptions.DuplicatedNameValidationError;
import io.imunity.furms.api.validation.exceptions.IdNotFoundValidationError;
import io.imunity.furms.domain.resource_credit_allocation.ResourceCreditAllocation;
import io.imunity.furms.spi.communites.CommunityRepository;
import io.imunity.furms.spi.resource_credit_allocation.ResourceCreditAllocationRepository;
import io.imunity.furms.spi.resource_credits.ResourceCreditRepository;
import io.imunity.furms.spi.resource_type.ResourceTypeRepository;
import io.imunity.furms.spi.sites.SiteRepository;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.Optional;

import static io.imunity.furms.core.constant.ValidationConst.MAX_NAME_LENGTH;
import static io.imunity.furms.utils.ValidationUtils.check;
import static org.springframework.util.Assert.notNull;

@Component
class ResourceCreditAllocationServiceValidator {
	private final ResourceCreditAllocationRepository resourceCreditAllocationRepository;
	private final ResourceCreditRepository resourceCreditRepository;
	private final ResourceTypeRepository resourceTypeRepository;
	private final CommunityRepository communityRepository;
	private final SiteRepository siteRepository;

	ResourceCreditAllocationServiceValidator(ResourceCreditAllocationRepository resourceCreditAllocationRepository,
	                                         ResourceCreditRepository resourceCreditRepository,
	                                         ResourceTypeRepository resourceTypeRepository,
	                                         CommunityRepository communityRepository,
	                                         SiteRepository siteRepository) {
		this.resourceCreditAllocationRepository = resourceCreditAllocationRepository;
		this.resourceCreditRepository = resourceCreditRepository;
		this.resourceTypeRepository = resourceTypeRepository;
		this.communityRepository = communityRepository;
		this.siteRepository = siteRepository;
	}

	void validateCreate(ResourceCreditAllocation resourceCreditAllocation) {
		notNull(resourceCreditAllocation, "ResourceCreditAllocation object cannot be null.");
		validateSiteId(resourceCreditAllocation.siteId);
		validateCommunityId(resourceCreditAllocation.communityId);
		validateResourceTypeId(resourceCreditAllocation.resourceTypeId);
		validateResourceCreditId(resourceCreditAllocation.resourceCreditId);
		validateName(resourceCreditAllocation);
		notNull(resourceCreditAllocation.amount, "ResourceCreditAllocation amount cannot be null.");
	}

	void validateUpdate(ResourceCreditAllocation resourceCreditAllocation) {
		notNull(resourceCreditAllocation, "ResourceCreditAllocation object cannot be null.");
		validateId(resourceCreditAllocation.id);
		validateUpdateSiteId(resourceCreditAllocation);
		validateUpdateCommunityId(resourceCreditAllocation);
		validateUpdateResourceTypeId(resourceCreditAllocation);
		validateUpdateResourceCreditId(resourceCreditAllocation);
		validateName(resourceCreditAllocation);
		notNull(resourceCreditAllocation.amount, "ResourceCreditAllocation amount cannot be null.");
	}

	void validateDelete(String id) {
		validateId(id);
	}

	private void validateName(ResourceCreditAllocation resourceCreditAllocation) {
		notNull(resourceCreditAllocation.name, "ResourceCreditAllocation name has to be declared.");
		validateLength("name", resourceCreditAllocation.name, MAX_NAME_LENGTH);
		if (isNameUnique(resourceCreditAllocation)) {
			throw new DuplicatedNameValidationError("ResourceCreditAllocation name has to be unique.");
		}
	}

	private boolean isNameUnique(ResourceCreditAllocation resourceCreditAllocation) {
		Optional<ResourceCreditAllocation> optionalProject = resourceCreditAllocationRepository.findById(resourceCreditAllocation.id);
		return !resourceCreditAllocationRepository.isUniqueName(resourceCreditAllocation.name) &&
			(optionalProject.isEmpty() || !optionalProject.get().name.equals(resourceCreditAllocation.name));
	}

	private void validateLength(String fieldName, String fieldVale, int length) {
		if (Objects.nonNull(fieldVale) && fieldVale.length() > length) {
			throw new IllegalArgumentException("ResourceCreditAllocation " + fieldName + " is too long.");
		}
	}

	private void validateId(String id) {
		notNull(id, "Resource CreditAllocation ID has to be declared.");
		check(resourceCreditAllocationRepository.exists(id), () -> new IdNotFoundValidationError("ResourceCreditAllocation with declared ID is not exists."));
	}

	private void validateSiteId(String id) {
		notNull(id, "Site ID has to be declared.");
		check(siteRepository.exists(id), () -> new IdNotFoundValidationError("Site with declared ID is not exists."));
	}

	private void validateCommunityId(String id) {
		notNull(id, "Site ID has to be declared.");
		check(communityRepository.exists(id), () -> new IdNotFoundValidationError("Community with declared ID is not exists."));
	}

	private void validateResourceTypeId(String id) {
		notNull(id, "ResourceType ID has to be declared.");
		check(resourceTypeRepository.exists(id), () -> new IdNotFoundValidationError("ResourceType with declared ID does not exist."));
	}

	private void validateResourceCreditId(String id) {
		notNull(id, "ResourceType ID has to be declared.");
		check(resourceCreditRepository.exists(id), () -> new IdNotFoundValidationError("ResourceCredit with declared ID does not exist."));
	}

	private void validateUpdateSiteId(ResourceCreditAllocation resourceCreditAllocation) {
		validateSiteId(resourceCreditAllocation.siteId);
		resourceCreditAllocationRepository.findById(resourceCreditAllocation.id)
			.map(s -> s.siteId)
			.filter(id -> id.equals(resourceCreditAllocation.siteId))
			.orElseThrow(() -> new IllegalArgumentException("Site ID change is forbidden"));
	}

	private void validateUpdateCommunityId(ResourceCreditAllocation resourceCreditAllocation) {
		validateCommunityId(resourceCreditAllocation.communityId);
		resourceCreditAllocationRepository.findById(resourceCreditAllocation.id)
			.map(s -> s.communityId)
			.filter(id -> id.equals(resourceCreditAllocation.communityId))
			.orElseThrow(() -> new IllegalArgumentException("Community ID change is forbidden"));
	}

	private void validateUpdateResourceTypeId(ResourceCreditAllocation resourceCreditAllocation) {
		validateResourceTypeId(resourceCreditAllocation.resourceTypeId);
		resourceCreditAllocationRepository.findById(resourceCreditAllocation.id)
			.map(s -> s.resourceTypeId)
			.filter(id -> id.equals(resourceCreditAllocation.resourceTypeId))
			.orElseThrow(() -> new IllegalArgumentException("Resource Type ID change is forbidden"));
	}

	private void validateUpdateResourceCreditId(ResourceCreditAllocation resourceCreditAllocation) {
		validateResourceCreditId(resourceCreditAllocation.resourceCreditId);
		resourceCreditAllocationRepository.findById(resourceCreditAllocation.id)
			.map(s -> s.resourceCreditId)
			.filter(id -> id.equals(resourceCreditAllocation.resourceCreditId))
			.orElseThrow(() -> new IllegalArgumentException("Resource Credit ID change is forbidden"));
	}
}
