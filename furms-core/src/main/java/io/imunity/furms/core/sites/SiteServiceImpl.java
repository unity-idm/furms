/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.core.sites;

import io.imunity.furms.api.sites.SiteService;
import io.imunity.furms.core.config.security.method.FurmsAuthorize;
import io.imunity.furms.domain.sites.Site;
import io.imunity.furms.spi.sites.SiteRepository;
import io.imunity.furms.spi.sites.SiteWebClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.Set;

import static io.imunity.furms.domain.authz.roles.Capability.SITE_READ;
import static io.imunity.furms.domain.authz.roles.Capability.SITE_WRITE;
import static io.imunity.furms.domain.authz.roles.ResourceType.SITE;

@Service
class SiteServiceImpl implements SiteService {

	private static final Logger LOG = LoggerFactory.getLogger(SiteServiceImpl.class);

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
	@FurmsAuthorize(capability = SITE_READ, resourceType = SITE, id = "id")
	public Optional<Site> findById(String id) {
		return siteRepository.findById(id);
	}

	@Override
	@FurmsAuthorize(capability = SITE_READ, resourceType = SITE)
	public Set<Site> findAll() {
		return siteRepository.findAll();
	}

	@Override
	@Transactional
	@FurmsAuthorize(capability = SITE_WRITE, resourceType = SITE)
	public void create(Site site) {
		validator.validateCreate(site);

		String siteId = siteRepository.create(site);
		Site createdSite = siteRepository.findById(siteId)
				.orElseThrow(() -> new IllegalStateException("Site has not been saved to DB correctly."));
		try {
			webClient.create(createdSite);
		} catch (RuntimeException e) {
			LOG.error("Could not create Site: ", e);
			throw e;
		}
	}

	@Override
	@Transactional
	@FurmsAuthorize(capability = SITE_WRITE, resourceType = SITE, id = "site.id")
	public void update(Site site) {
		validator.validateUpdate(site);

		String siteId = siteRepository.update(site);
		Site updatedSite = siteRepository.findById(siteId)
				.orElseThrow(() -> new IllegalStateException("Site has not been saved to DB correctly."));
		try {
			webClient.update(updatedSite);
		} catch (RuntimeException e) {
			LOG.error("Could not update Site: ", e);
			throw e;
		}
	}

	@Override
	@Transactional
	@FurmsAuthorize(capability = SITE_WRITE, resourceType = SITE)
	public void delete(String id) {
		validator.validateDelete(id);

		siteRepository.delete(id);
		try {
			webClient.delete(id);
		} catch (RuntimeException e) {
			LOG.error("Could not delete Site: ", e);
			throw e;
		}
	}

	@Override
	@FurmsAuthorize(capability = SITE_READ, resourceType = SITE)
	public boolean isNameUnique(String name) {
		try {
			validator.validateName(name);
			return true;
		} catch (IllegalArgumentException e) {
			return false;
		}
	}
}
