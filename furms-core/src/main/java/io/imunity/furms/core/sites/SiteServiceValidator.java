/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.core.sites;

import io.imunity.furms.api.validation.exceptions.DuplicatedNameValidationError;
import io.imunity.furms.api.validation.exceptions.IdNotFoundValidationError;
import io.imunity.furms.api.validation.exceptions.SiteHasResourceCreditsRemoveValidationError;
import io.imunity.furms.domain.sites.Site;
import io.imunity.furms.domain.sites.SiteId;
import io.imunity.furms.spi.resource_credits.ResourceCreditRepository;
import io.imunity.furms.spi.sites.SiteRepository;
import org.springframework.stereotype.Component;

import java.util.Objects;

import static io.imunity.furms.api.constant.ValidationConst.MAX_SITE_NAME_LENGTH;
import static io.imunity.furms.utils.ValidationUtils.assertTrue;
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

	void validateDelete(SiteId id) {
		validateId(id);
		if (resourceCreditRepository.existsBySiteId(id)) {
			throw new SiteHasResourceCreditsRemoveValidationError("Site should not have ResourceCredits.");
		}
	}

	void validateName(String name) {
		notNull(name, "Site name has to be declared.");
		validateLength(name, MAX_SITE_NAME_LENGTH);
		assertTrue(!siteRepository.isNamePresent(name), () -> new DuplicatedNameValidationError("Site name has to be unique."));
	}

	private void validateLength(String fieldVale, int length) {
		if (Objects.nonNull(fieldVale) && fieldVale.length() > length) {
			throw new IllegalArgumentException("Site name is too long.");
		}
	}

	void validateIsNamePresentIgnoringRecord(String name, SiteId siteId) {
		notNull(siteId, "Site id has to be declared.");
		notNull(siteId.id, "Site id has to be declared.");
		notNull(name, "Invalid Site name: Site name is empty.");
		assertTrue(!siteRepository.isNamePresentIgnoringRecord(name, siteId), () -> new DuplicatedNameValidationError("Invalid Site name: Site name has to be unique."));
	}

	private void validateId(SiteId id) {
		notNull(id, "Site ID has to be declared.");
		assertTrue(siteRepository.exists(id), () -> new IdNotFoundValidationError("Site with declared ID is not exists."));
	}

}
