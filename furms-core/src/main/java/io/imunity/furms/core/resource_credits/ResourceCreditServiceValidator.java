/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.core.resource_credits;

import io.imunity.furms.api.validation.exceptions.ResourceTypeCreditHasAllocationsRemoveValidationError;
import io.imunity.furms.api.validation.exceptions.DuplicatedNameValidationError;
import io.imunity.furms.api.validation.exceptions.IdNotFoundValidationError;
import io.imunity.furms.domain.resource_credits.ResourceCredit;
import io.imunity.furms.spi.resource_credit_allocation.ResourceCreditAllocationRepository;
import io.imunity.furms.spi.resource_credits.ResourceCreditRepository;
import io.imunity.furms.spi.resource_type.ResourceTypeRepository;
import io.imunity.furms.spi.sites.SiteRepository;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Optional;

import static io.imunity.furms.core.constant.ValidationConst.MAX_NAME_LENGTH;
import static io.imunity.furms.utils.ValidationUtils.check;
import static org.springframework.util.Assert.notNull;

@Component
class ResourceCreditServiceValidator {
	private final ResourceCreditAllocationRepository resourceCreditAllocationRepository;
	private final ResourceCreditRepository resourceCreditRepository;
	private final ResourceTypeRepository resourceTypeRepository;
	private final SiteRepository siteRepository;

	public ResourceCreditServiceValidator(
		ResourceCreditAllocationRepository resourceCreditAllocationRepository,
		ResourceCreditRepository resourceCreditRepository,
		ResourceTypeRepository resourceTypeRepository,
		SiteRepository siteRepository
	) {
		this.resourceCreditAllocationRepository = resourceCreditAllocationRepository;
		this.resourceCreditRepository = resourceCreditRepository;
		this.resourceTypeRepository = resourceTypeRepository;
		this.siteRepository = siteRepository;
	}

	void validateCreate(ResourceCredit resourceCredit) {
		notNull(resourceCredit, "ResourceCredit object cannot be null.");
		validateSiteId(resourceCredit.siteId);
		validateResourceTypeId(resourceCredit.resourceTypeId);
		validateName(resourceCredit);
		notNull(resourceCredit.amount, "ResourceCredit amount cannot be null.");
		validateCreateTime(resourceCredit.utcCreateTime);
		validateTime(resourceCredit.utcStartTime, resourceCredit.utcEndTime);
	}

	void validateUpdate(ResourceCredit resourceCredit) {
		notNull(resourceCredit, "ResourceCredit object cannot be null.");
		validateId(resourceCredit.id);
		validateUpdateSiteId(resourceCredit);
		validateResourceTypeId(resourceCredit.resourceTypeId);
		validateName(resourceCredit);
		notNull(resourceCredit.amount, "ResourceCredit amount cannot be null.");
		validateCreateTime(resourceCredit.utcCreateTime);
		validateTime(resourceCredit.utcStartTime, resourceCredit.utcEndTime);
	}

	void validateDelete(String id) {
		validateId(id);
		if(resourceCreditAllocationRepository.existsByResourceCreditId(id)){
			throw new ResourceTypeCreditHasAllocationsRemoveValidationError("ResourceTypeCredit should not have ResourceCreditAllocations.");
		}
	}

	private void validateName(ResourceCredit resourceCredit) {
		notNull(resourceCredit.name, "ResourceCredit name has to be declared.");
		validateLength("name", resourceCredit.name, MAX_NAME_LENGTH);
		if (isNameUnique(resourceCredit)) {
			throw new DuplicatedNameValidationError("ResourceCredit name has to be unique.");
		}
	}

	private boolean isNameUnique(ResourceCredit resourceCredit) {
		Optional<ResourceCredit> optionalProject = resourceCreditRepository.findById(resourceCredit.id);
		return !resourceCreditRepository.isUniqueName(resourceCredit.name) &&
			(optionalProject.isEmpty() || !optionalProject.get().name.equals(resourceCredit.name));
	}

	private void validateLength(String fieldName, String fieldVale, int length) {
		if (Objects.nonNull(fieldVale) && fieldVale.length() > length) {
			throw new IllegalArgumentException("ResourceCredit " + fieldName + " is too long.");
		}
	}

	private void validateId(String id) {
		notNull(id, "Resource Credit ID has to be declared.");
		check(resourceCreditRepository.exists(id), () -> new IdNotFoundValidationError("ResourceCredit with declared ID is not exists."));
	}

	private void validateSiteId(String id) {
		notNull(id, "Site ID has to be declared.");
		check(siteRepository.exists(id), () -> new IdNotFoundValidationError("Site with declared ID is not exists."));
	}

	private void validateResourceTypeId(String id) {
		notNull(id, "ResourceType ID has to be declared.");
		check(resourceTypeRepository.exists(id), () -> new IdNotFoundValidationError("ResourceType with declared ID does not exist."));
	}

	private void validateUpdateSiteId(ResourceCredit resourceCredit) {
		validateSiteId(resourceCredit.siteId);
		resourceCreditRepository.findById(resourceCredit.id)
			.map(s -> s.siteId)
			.filter(id -> id.equals(resourceCredit.siteId))
			.orElseThrow(() -> new IllegalArgumentException("Site ID change is forbidden"));
	}

	private void validateCreateTime(LocalDateTime createTime) {
		notNull(createTime, "ResourceCredit create time cannot be null");
		if(createTime.isAfter(LocalDateTime.now())){
			throw new IllegalArgumentException("ResourceCredit createTime time must be earlier than now");
		}
	}

	private void validateTime(LocalDateTime startTime, LocalDateTime endTime) {
		notNull(startTime, "ResourceCredit start time cannot be null");
		notNull(endTime, "ResourceCredit start time cannot be null");
		if(startTime.isAfter(endTime)){
			throw new IllegalArgumentException("ResourceCredit start time must be earlier than end time");
		}
	}
}
