/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.core.services;

import io.imunity.furms.api.validation.exceptions.DuplicatedNameValidationError;
import io.imunity.furms.api.validation.exceptions.IdNotFoundValidationError;
import io.imunity.furms.api.validation.exceptions.InfraServiceHasIndirectlyResourceCreditsRemoveValidationError;
import io.imunity.furms.domain.services.InfraService;
import io.imunity.furms.spi.resource_credits.ResourceCreditRepository;
import io.imunity.furms.spi.resource_type.ResourceTypeRepository;
import io.imunity.furms.spi.services.InfraServiceRepository;
import io.imunity.furms.spi.sites.SiteRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import static io.imunity.furms.core.constant.ValidationConst.MAX_DESCRIPTION_LENGTH;
import static io.imunity.furms.core.constant.ValidationConst.MAX_NAME_LENGTH;
import static io.imunity.furms.utils.ValidationUtils.assertTrue;
import static org.springframework.util.Assert.notNull;

@Component
class InfraServiceServiceValidator {
	private final InfraServiceRepository infraServiceRepository;
	private final SiteRepository siteRepository;
	private final ResourceTypeRepository resourceTypeRepository;
	private final ResourceCreditRepository resourceCreditRepository;

	InfraServiceServiceValidator(InfraServiceRepository infraServiceRepository, SiteRepository siteRepository,
	                             ResourceTypeRepository resourceTypeRepository, ResourceCreditRepository resourceCreditRepository) {
		this.infraServiceRepository = infraServiceRepository;
		this.siteRepository = siteRepository;
		this.resourceTypeRepository = resourceTypeRepository;
		this.resourceCreditRepository = resourceCreditRepository;
	}

	void validateCreate(InfraService infraService) {
		notNull(infraService, "InfraService object cannot be null.");
		validateSiteId(infraService.siteId);
		validateName(infraService);
		validateLength("description", infraService.description, MAX_DESCRIPTION_LENGTH);
	}

	void validateUpdate(InfraService infraService) {
		notNull(infraService, "InfraService object cannot be null.");
		validateId(infraService.id);
		validateUpdateSiteId(infraService);
		validateName(infraService);
		validateLength("description", infraService.description, MAX_DESCRIPTION_LENGTH);
	}

	void validateDelete(String id) {
		validateId(id);
		List<String> resourceTypeIds = resourceTypeRepository.findAllByInfraServiceId(id).stream()
			.map(resourceType -> resourceType.id)
			.collect(Collectors.toList());
		if (resourceCreditRepository.existsByResourceTypeIdIn(resourceTypeIds)) {
			throw new InfraServiceHasIndirectlyResourceCreditsRemoveValidationError("InfraService should not have ResourceType, which has ResourceCredits.");
		}
	}

	private void validateName(InfraService infraService) {
		notNull(infraService.name, "InfraService name has to be declared.");
		validateLength("name", infraService.name, MAX_NAME_LENGTH);
		if (isNameOccupied(infraService)) {
			throw new DuplicatedNameValidationError("InfraService name has to be unique.");
		}
	}

	private boolean isNameOccupied(InfraService infraService) {
		Optional<InfraService> existingService = infraServiceRepository.findById(infraService.id);
		if (existingService.isPresent() && existingService.get().name.equals(infraService.name))
			return false;
		return infraServiceRepository.isNamePresent(infraService.name, infraService.siteId);
	}

	private void validateLength(String fieldName, String fieldVale, int length) {
		if (Objects.nonNull(fieldVale) && fieldVale.length() > length) {
			throw new IllegalArgumentException("InfraService " + fieldName + " is too long.");
		}
	}

	private void validateId(String id) {
		notNull(id, "InfraService ID has to be declared.");
		assertTrue(infraServiceRepository.exists(id), () -> new IdNotFoundValidationError("Service with declared ID is not exists."));
	}

	private void validateSiteId(String id) {
		notNull(id, "Site ID has to be declared.");
		assertTrue(siteRepository.exists(id), () -> new IdNotFoundValidationError("Site with declared ID does not exist."));
	}

	private void validateUpdateSiteId(InfraService infraService) {
		validateSiteId(infraService.siteId);
		infraServiceRepository.findById(infraService.id)
			.map(s -> s.siteId)
			.filter(id -> id.equals(infraService.siteId))
			.orElseThrow(() -> new IllegalArgumentException("Site ID change is forbidden"));
	}
}
