/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.core.sites;

import io.imunity.furms.api.sites.exceptions.DuplicatedNameValidationError;
import io.imunity.furms.api.sites.exceptions.IdNotFoundValidationError;
import io.imunity.furms.domain.sites.Site;
import io.imunity.furms.spi.sites.SiteRepository;
import org.springframework.stereotype.Component;

import static org.springframework.util.Assert.notNull;

@Component
class SiteServiceValidator {

	private final SiteRepository siteRepository;

	SiteServiceValidator(SiteRepository siteRepository) {
		this.siteRepository = siteRepository;
	}

	void validateCreate(Site site) {
		notNull(site, "Site object cannot be null.");
		validateName(site.getName());
	}

	void validateUpdate(Site request) {
		notNull(request, "Site object cannot be null.");
		validateId(request.getId());
		validateUniqueName(request.getId(), request.getName());
	}

	void validateDelete(String id) {
		validateId(id);
	}

	void validateName(String name) {
		notNull(name, "Site name has to be declared.");
		if (!siteRepository.isUniqueName(name)) {
			throw new DuplicatedNameValidationError("Site name has to be unique.");
		}
	}

	void validateUniqueName(String id, String name) {
		notNull(id, "Site id has to be declared.");
		notNull(name, "Site name has to be declared.");

		if (!siteRepository.isUniqueName(id, name)) {
			throw new DuplicatedNameValidationError("Site name has to be unique.");
		}
	}

	private void validateId(String id) {
		notNull(id, "Site ID has to be declared.");
		if (!siteRepository.exists(id)) {
			throw new IdNotFoundValidationError("Site with declared ID is not exists.");
		}
	}

}
