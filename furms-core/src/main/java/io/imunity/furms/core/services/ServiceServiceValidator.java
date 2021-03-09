/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.core.services;

import io.imunity.furms.api.validation.exceptions.DuplicatedNameValidationError;
import io.imunity.furms.api.validation.exceptions.IdNotFoundValidationError;
import io.imunity.furms.domain.services.Service;
import io.imunity.furms.spi.services.ServiceRepository;
import io.imunity.furms.spi.sites.SiteRepository;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.Optional;

import static io.imunity.furms.core.constant.ValidationConst.MAX_DESCRIPTION_LENGTH;
import static io.imunity.furms.core.constant.ValidationConst.MAX_NAME_LENGTH;
import static io.imunity.furms.utils.ValidationUtils.check;
import static org.springframework.util.Assert.notNull;

@Component
public class ServiceServiceValidator {
	private final ServiceRepository serviceRepository;
	private final SiteRepository siteRepository;

	public ServiceServiceValidator(ServiceRepository serviceRepository, SiteRepository siteRepository) {
		this.serviceRepository = serviceRepository;
		this.siteRepository = siteRepository;
	}

	void validateCreate(Service service) {
		notNull(service, "Service object cannot be null.");
		validateSiteId(service.siteId);
		validateName(service);
		validateLength("description", service.description, MAX_DESCRIPTION_LENGTH);
	}

	void validateUpdate(Service service) {
		notNull(service, "Service object cannot be null.");
		validateId(service.id);
		validateUpdateSiteId(service);
		validateName(service);
		validateLength("description", service.description, MAX_DESCRIPTION_LENGTH);
	}

	void validateDelete(String id) {
		validateId(id);
	}

	private void validateName(Service service) {
		notNull(service.name, "Project name has to be declared.");
		validateLength("name", service.name, MAX_NAME_LENGTH);
		if (isNameUnique(service)) {
			throw new DuplicatedNameValidationError("Project name has to be unique.");
		}
	}

	private boolean isNameUnique(Service service) {
		Optional<Service> optionalProject = serviceRepository.findById(service.id);
		return !serviceRepository.isUniqueName(service.name) &&
			(optionalProject.isEmpty() || !optionalProject.get().name.equals(service.name));
	}

	private void validateLength(String fieldName, String fieldVale, int length) {
		if (Objects.nonNull(fieldVale) && fieldVale.length() > length) {
			throw new IllegalArgumentException("Service " + fieldName + " is too long.");
		}
	}

	private void validateId(String id) {
		notNull(id, "Project ID has to be declared.");
		check(serviceRepository.exists(id), () -> new IdNotFoundValidationError("Service with declared ID is not exists."));
	}

	private void validateSiteId(String id) {
		notNull(id, "Site ID has to be declared.");
		check(siteRepository.exists(id), () -> new IdNotFoundValidationError("Site with declared ID is not exists."));
	}

	private void validateUpdateSiteId(Service service) {
		validateSiteId(service.siteId);
		serviceRepository.findById(service.id)
			.map(s -> s.siteId)
			.filter(id -> id.equals(service.siteId))
			.orElseThrow(() -> new IllegalArgumentException("Site ID change is forbidden"));
	}
}
