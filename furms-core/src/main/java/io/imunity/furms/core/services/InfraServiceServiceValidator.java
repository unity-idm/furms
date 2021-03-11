/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.core.services;

import io.imunity.furms.api.validation.exceptions.DuplicatedNameValidationError;
import io.imunity.furms.api.validation.exceptions.IdNotFoundValidationError;
import io.imunity.furms.domain.services.InfraService;
import io.imunity.furms.spi.services.InfraServiceRepository;
import io.imunity.furms.spi.sites.SiteRepository;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.Optional;

import static io.imunity.furms.core.constant.ValidationConst.MAX_DESCRIPTION_LENGTH;
import static io.imunity.furms.core.constant.ValidationConst.MAX_NAME_LENGTH;
import static io.imunity.furms.utils.ValidationUtils.check;
import static org.springframework.util.Assert.notNull;

@Component
class InfraServiceServiceValidator {
	private final InfraServiceRepository infraServiceRepository;
	private final SiteRepository siteRepository;

	InfraServiceServiceValidator(InfraServiceRepository infraServiceRepository, SiteRepository siteRepository) {
		this.infraServiceRepository = infraServiceRepository;
		this.siteRepository = siteRepository;
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
	}

	private void validateName(InfraService infraService) {
		notNull(infraService.name, "InfraService name has to be declared.");
		validateLength("name", infraService.name, MAX_NAME_LENGTH);
		if (isNameUnique(infraService)) {
			throw new DuplicatedNameValidationError("InfraService name has to be unique.");
		}
	}

	private boolean isNameUnique(InfraService infraService) {
		Optional<InfraService> optionalProject = infraServiceRepository.findById(infraService.id);
		return !infraServiceRepository.isUniqueName(infraService.name) &&
			(optionalProject.isEmpty() || !optionalProject.get().name.equals(infraService.name));
	}

	private void validateLength(String fieldName, String fieldVale, int length) {
		if (Objects.nonNull(fieldVale) && fieldVale.length() > length) {
			throw new IllegalArgumentException("InfraService " + fieldName + " is too long.");
		}
	}

	private void validateId(String id) {
		notNull(id, "InfraService ID has to be declared.");
		check(infraServiceRepository.exists(id), () -> new IdNotFoundValidationError("Service with declared ID is not exists."));
	}

	private void validateSiteId(String id) {
		notNull(id, "Site ID has to be declared.");
		check(siteRepository.exists(id), () -> new IdNotFoundValidationError("Site with declared ID does not exist."));
	}

	private void validateUpdateSiteId(InfraService infraService) {
		validateSiteId(infraService.siteId);
		infraServiceRepository.findById(infraService.id)
			.map(s -> s.siteId)
			.filter(id -> id.equals(infraService.siteId))
			.orElseThrow(() -> new IllegalArgumentException("Site ID change is forbidden"));
	}
}
