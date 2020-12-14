/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.core.sites;

import io.imunity.furms.domain.sites.Site;
import io.imunity.furms.spi.sites.SiteRepository;
import org.springframework.stereotype.Component;

import static org.springframework.util.Assert.notNull;

@Component
class SiteServiceAPIValidator {

	private final SiteRepository siteRepository;

	SiteServiceAPIValidator(SiteRepository siteRepository) {
		this.siteRepository = siteRepository;
	}

	void validateCreate(Site site) {
		notNull(site, "Site object cannot be null.");
		validateName(site);
	}

	void validateUpdate(Site request) {
		notNull(request, "Site object cannot be null.");
		validateId(request.getId());
		validateName(request);
	}

	void validateDelete(String id) {
		validateId(id);
	}

	private void validateName(Site site) {
		notNull(site.getName(), "Site name has to be declared.");
		if (!siteRepository.isUniqueName(site.getName())) {
			throw new IllegalArgumentException("Site name has to be unique.");
		}
	}

	private void validateId(String id) {
		notNull(id, "Site ID has to be declared.");
		if (!siteRepository.exists(id)) {
			throw new IllegalArgumentException("Site with declared ID is not exists.");
		}
	}

}
