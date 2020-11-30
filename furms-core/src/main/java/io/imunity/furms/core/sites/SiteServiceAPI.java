/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.core.sites;

import io.imunity.furms.api.sites.SiteService;
import io.imunity.furms.domain.sites.Site;
import io.imunity.furms.spi.sites.SiteRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.Set;

@Service
class SiteServiceAPI implements SiteService {

	private final SiteRepository siteRepository;
	private final SiteServiceAPIValidator validator;

	SiteServiceAPI(SiteRepository siteRepository,
	               SiteServiceAPIValidator validator) {
		this.siteRepository = siteRepository;
		this.validator = validator;
	}

	@Override
	public Optional<Site> findById(String id) {
		return siteRepository.findById(id);
	}

	@Override
	public Set<Site> findAll() {
		return siteRepository.findAll();
	}

	@Override
	public void create(Site site) {
		validator.validateCreate(site);

		siteRepository.save(site);
	}

	@Override
	public void update(Site site) {
		validator.validateUpdate(site);

		siteRepository.save(site);
	}

	@Override
	public void delete(String id) {
		validator.validateDelete(id);

		siteRepository.delete(id);
	}
}
