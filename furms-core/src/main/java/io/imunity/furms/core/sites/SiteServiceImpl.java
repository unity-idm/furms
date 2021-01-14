/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.core.sites;

import io.imunity.furms.api.sites.SiteService;
import io.imunity.furms.domain.sites.Site;
import io.imunity.furms.spi.sites.SiteRepository;
import io.imunity.furms.spi.sites.SiteWebClient;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.Set;

@Service
class SiteServiceImpl implements SiteService {

	private final SiteRepository siteRepository;
	private final SiteServiceValidator validator;
	private final SiteWebClient webClient;

	SiteServiceImpl(SiteRepository siteRepository,
	                SiteServiceValidator validator,
	                SiteWebClient webClient) {
		this.siteRepository = siteRepository;
		this.validator = validator;
		this.webClient = webClient;
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

		String siteId = siteRepository.create(site);
		Site createdSite = siteRepository.findById(siteId)
				.orElseThrow(() -> new IllegalStateException("Site has not been saved to DB correctly."));
		webClient.create(createdSite);
	}

	@Override
	public void update(Site site) {
		validator.validateUpdate(site);

		String siteId = siteRepository.update(site);
		Site updatedSite = siteRepository.findById(siteId)
				.orElseThrow(() -> new IllegalStateException("Site has not been saved to DB correctly."));
		webClient.update(updatedSite);
	}

	@Override
	public void delete(String id) {
		validator.validateDelete(id);

		siteRepository.delete(id);
		webClient.delete(id);
	}

	@Override
	public boolean isNameUnique(String name) {
		try {
			validator.validateName(name);
			return true;
		} catch (IllegalArgumentException e) {
			return false;
		}
	}
}
