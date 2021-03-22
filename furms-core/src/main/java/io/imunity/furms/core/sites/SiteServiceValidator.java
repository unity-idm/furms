/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.core.sites;

import io.imunity.furms.api.validation.exceptions.DuplicatedNameValidationError;
import io.imunity.furms.api.validation.exceptions.IdNotFoundValidationError;
import io.imunity.furms.api.validation.exceptions.SiteHasResourceCreditsRemoveValidationError;
import io.imunity.furms.domain.sites.Site;
import io.imunity.furms.spi.resource_credits.ResourceCreditRepository;
import io.imunity.furms.spi.sites.SiteRepository;
import org.springframework.stereotype.Component;

import static io.imunity.furms.utils.ValidationUtils.check;
import static org.springframework.util.Assert.notNull;

@Component
class SiteServiceValidator {

	private final SiteRepository siteRepository;
	private final ResourceCreditRepository resourceCreditRepository;

	SiteServiceValidator(SiteRepository siteRepository, ResourceCreditRepository resourceCreditRepository) {
		this.siteRepository = siteRepository;
		this.resourceCreditRepository = resourceCreditRepository;
	}

	void validateCreate(Site site) {
		notNull(site, "Site object cannot be null.");
		validateName(site.getName());
	}

	void validateUpdate(Site request) {
		notNull(request, "Site object cannot be null.");
		validateId(request.getId());
		validateIsNamePresentIgnoringRecord(request.getName(), request.getId());
	}

	void validateDelete(String id) {
		validateId(id);
		if (resourceCreditRepository.existsBySiteId(id)) {
			throw new SiteHasResourceCreditsRemoveValidationError("Site should not have ResourceCredits.");
		}
	}

	void validateName(String name) {
		notNull(name, "Site name has to be declared.");
		check(!siteRepository.isNamePresent(name), () -> new DuplicatedNameValidationError("Site name has to be unique."));
	}

	void validateIsNamePresentIgnoringRecord(String name, String recordToIgnore) {
		notNull(recordToIgnore, "Site id has to be declared.");
		notNull(name, "Invalid Site name: Site name is empty.");
		check(!siteRepository.isNamePresentIgnoringRecord(name, recordToIgnore), () -> new DuplicatedNameValidationError("Invalid Site name: Site name has to be unique."));
	}

	private void validateId(String id) {
		notNull(id, "Site ID has to be declared.");
		check(siteRepository.exists(id), () -> new IdNotFoundValidationError("Site with declared ID is not exists."));
	}

}
