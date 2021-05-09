/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.core.resource_types;

import static io.imunity.furms.core.constant.ValidationConst.MAX_NAME_LENGTH;
import static io.imunity.furms.utils.ValidationUtils.assertTrue;
import static org.springframework.util.Assert.notNull;

import java.util.Objects;
import java.util.Optional;

import org.springframework.stereotype.Component;

import io.imunity.furms.api.validation.exceptions.DuplicatedNameValidationError;
import io.imunity.furms.api.validation.exceptions.IdNotFoundValidationError;
import io.imunity.furms.api.validation.exceptions.ResourceTypeHasResourceCreditsRemoveValidationError;
import io.imunity.furms.api.validation.exceptions.TypeAndUnitAreInconsistentValidationError;
import io.imunity.furms.domain.resource_types.ResourceType;
import io.imunity.furms.spi.resource_credits.ResourceCreditRepository;
import io.imunity.furms.spi.resource_type.ResourceTypeRepository;
import io.imunity.furms.spi.services.InfraServiceRepository;
import io.imunity.furms.spi.sites.SiteRepository;

@Component
class ResourceTypeServiceValidator {
	private final ResourceTypeRepository resourceTypeRepository;
	private final ResourceCreditRepository resourceCreditRepository;
	private final InfraServiceRepository infraServiceRepository;
	private final SiteRepository siteRepository;

	public ResourceTypeServiceValidator(
		ResourceTypeRepository resourceTypeRepository,
		ResourceCreditRepository resourceCreditRepository,
		InfraServiceRepository infraServiceRepository,
		SiteRepository siteRepository
	) {
		this.resourceTypeRepository = resourceTypeRepository;
		this.infraServiceRepository = infraServiceRepository;
		this.resourceCreditRepository = resourceCreditRepository;
		this.siteRepository = siteRepository;
	}

	void validateCreate(ResourceType resourceType) {
		notNull(resourceType, "ResourceType object cannot be null.");
		validateSiteId(resourceType.siteId);
		validateServiceId(resourceType.serviceId);
		validateName(resourceType);
		validateTypeAndUnitConsistency(resourceType);
	}

	void validateUpdate(ResourceType resourceType) {
		notNull(resourceType, "ResourceType object cannot be null.");
		validateId(resourceType.id);
		validateUpdateSiteId(resourceType);
		validateServiceId(resourceType.serviceId);
		validateName(resourceType);
		validateTypeAndUnitConsistency(resourceType);
	}

	void validateDelete(String id) {
		validateId(id);
		if (resourceCreditRepository.existsByResourceTypeId(id)) {
			throw new ResourceTypeHasResourceCreditsRemoveValidationError("ResourceType should not have ResourceCredits.");
		}
	}

	private void validateName(ResourceType resourceType) {
		notNull(resourceType.name, "ResourceType name has to be declared.");
		validateLength("name", resourceType.name, MAX_NAME_LENGTH);
		if (isNameOccupied(resourceType)) {
			throw new DuplicatedNameValidationError("ResourceType name has to be unique.");
		}
	}

	private void validateTypeAndUnitConsistency(ResourceType resourceType) {
		notNull(resourceType.type, "ResourceType type has to be declared.");
		notNull(resourceType.unit, "ResourceType unit has to be declared.");
		if(!resourceType.type.units.contains(resourceType.unit)){
			throw new TypeAndUnitAreInconsistentValidationError("ResourceType type and unit has to be consistent.");
		}
	}

	private boolean isNameOccupied(ResourceType resourceType) {
		Optional<ResourceType> existingResource = resourceTypeRepository.findById(resourceType.id);
		if (existingResource.isPresent() && existingResource.get().name.equals(resourceType.name))
			return false;
		return resourceTypeRepository.isNamePresent(resourceType.name, resourceType.siteId);
	}
	
	private void validateLength(String fieldName, String fieldVale, int length) {
		if (Objects.nonNull(fieldVale) && fieldVale.length() > length) {
			throw new IllegalArgumentException("ResourceType " + fieldName + " is too long.");
		}
	}

	private void validateId(String id) {
		notNull(id, "Resource Type ID has to be declared.");
		assertTrue(resourceTypeRepository.exists(id), () -> new IdNotFoundValidationError("ResourceType with declared ID does not exist."));
	}

	private void validateSiteId(String id) {
		notNull(id, "Site ID has to be declared.");
		assertTrue(siteRepository.exists(id), () -> new IdNotFoundValidationError("Site with declared ID is not exists."));
	}

	private void validateServiceId(String id) {
		notNull(id, "Service ID has to be declared.");
		assertTrue(infraServiceRepository.exists(id), () -> new IdNotFoundValidationError("Service with declared ID is not exists."));
	}

	private void validateUpdateSiteId(ResourceType resourceType) {
		validateSiteId(resourceType.siteId);
		resourceTypeRepository.findById(resourceType.id)
			.map(s -> s.siteId)
			.filter(id -> id.equals(resourceType.siteId))
			.orElseThrow(() -> new IllegalArgumentException("Site ID change is forbidden"));
	}
}
