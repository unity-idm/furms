/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.core.sites;

import io.imunity.furms.api.sites.SiteService;
import io.imunity.furms.core.config.security.method.FurmsAuthorize;
import io.imunity.furms.domain.sites.Site;
import io.imunity.furms.domain.users.User;
import io.imunity.furms.spi.sites.SiteRepository;
import io.imunity.furms.spi.sites.SiteWebClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static io.imunity.furms.domain.authz.roles.Capability.SITE_READ;
import static io.imunity.furms.domain.authz.roles.Capability.SITE_WRITE;
import static io.imunity.furms.domain.authz.roles.ResourceType.SITE;
import static io.imunity.furms.utils.ValidationUtils.check;
import static java.util.Optional.ofNullable;
import static org.springframework.util.StringUtils.isEmpty;

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
		LOG.info("Getting Site with id={}", id);
		return siteRepository.findById(id);
	}

	@Override
	@FurmsAuthorize(capability = SITE_READ, resourceType = SITE)
	public Set<Site> findAll() {
		LOG.info("Getting all Sites");
		return siteRepository.findAll();
	}

	@Override
	@Transactional
	@FurmsAuthorize(capability = SITE_WRITE, resourceType = SITE)
	public void create(Site site) {
		validator.validateCreate(site);

		String siteId = siteRepository.create(site);
		LOG.info("Created Site in repository with ID={}", siteId);
		Site createdSite = siteRepository.findById(siteId)
				.orElseThrow(() -> new IllegalStateException("Site has not been saved to DB correctly."));
		try {
			webClient.create(createdSite);
			LOG.info("Created Site in Unity with ID={}", siteId);
		} catch (RuntimeException e) {
			LOG.error("Could not create Site: ", e);
			try {
				webClient.get(siteId).ifPresent(incompleteSite -> webClient.delete(incompleteSite.getId()));
			} catch (RuntimeException ex) {
				LOG.error("Failed to rollback, problem during unity group deletion: ", e);
			}
			throw e;
		}
	}

	@Override
	@Transactional
	@FurmsAuthorize(capability = SITE_WRITE, resourceType = SITE, id = "site.id")
	public void update(Site site) {
		validator.validateUpdate(site);

		Site oldSite = siteRepository.findById(site.getId()).get();

		String siteId = siteRepository.update(merge(oldSite, site));
		LOG.info("Updated Site in repository with ID={}, {}", siteId, site);
		Site updatedSite = siteRepository.findById(siteId)
				.orElseThrow(() -> new IllegalStateException("Site has not been saved to DB correctly."));
		try {
			webClient.update(updatedSite);
			LOG.info("Updated Site in Unity with ID={}", siteId);
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
		LOG.info("Removed Site from repository with ID={}", id);
		try {
			webClient.delete(id);
			LOG.info("Removed Site from Unity with ID={}", id);
		} catch (RuntimeException e) {
			LOG.error("Could not delete Site: ", e);
			throw e;
		}
	}

	@Override
	@FurmsAuthorize(capability = SITE_READ, resourceType = SITE)
	public boolean isNamePresent(String name) {
		try {
			validator.validateName(name);
			return true;
		} catch (IllegalArgumentException e) {
			return false;
		}
	}

	@Override
	@FurmsAuthorize(capability = SITE_READ, resourceType = SITE, id="recordToIgnore")
	public boolean isNamePresentIgnoringRecord(String name, String recordToIgnore) {
		try {
			validator.validateIsNamePresentIgnoringRecord(name, recordToIgnore);
			return false;
		} catch (IllegalArgumentException e) {
			return true;
		}
	}

	@Override
	@FurmsAuthorize(capability = SITE_WRITE, resourceType = SITE)
	public List<User> findAllAdmins(String id) {
		check(!isEmpty(id), () -> new IllegalArgumentException("Could not get Site Administrators. Missing Site ID."));
		LOG.info("Getting Site Administrators from Unity for Site ID={}", id);
		return webClient.getAllAdmins(id);
	}

	@Override
	@FurmsAuthorize(capability = SITE_WRITE, resourceType = SITE)
	public void addAdmin(String siteId, String userId) {
		check(!isEmpty(siteId) && !isEmpty(userId),
				() -> new IllegalArgumentException("Could not add Site Administrator. Missing Site ID or User ID"));

		try {
			webClient.addAdmin(siteId, userId);
			LOG.info("Added Site Administrator ({}) in Unity for Site ID={}", userId, siteId);
		} catch (RuntimeException e) {
			LOG.error("Could not add Site Administrator: ", e);
			try {
				webClient.get(siteId).ifPresent(incompleteSite -> webClient.removeAdmin(siteId, userId));
			} catch (RuntimeException ex) {
				LOG.error("Could not add Site Administrator: Failed to rollback, problem during unity group deletion: ", e);
			}
			throw e;
		}
	}

	@Override
	@FurmsAuthorize(capability = SITE_WRITE, resourceType = SITE)
	public void removeAdmin(String siteId, String userId) {
		check(!isEmpty(siteId) && !isEmpty(userId),
				() -> new IllegalArgumentException("Could not remove Site Administrator. Missing Site ID or User ID"));

		try {
			webClient.removeAdmin(siteId, userId);
			LOG.info("Removed Site Administrator ({}) from Unity for Site ID={}", userId, siteId);
		} catch (RuntimeException e) {
			LOG.error("Could not remove Site Administrator: ", e);
			throw e;
		}
	}

	private Site merge(Site oldSite, Site site) {
		check(oldSite.getId().equals(site.getId()),() -> new IllegalArgumentException("There are different Sites during merge"));
		return Site.builder()
				.id(oldSite.getId())
				.name(site.getName())
				.logo(ofNullable(site.getLogo()).orElse(oldSite.getLogo()))
				.connectionInfo(ofNullable(site.getConnectionInfo()).orElse(oldSite.getConnectionInfo()))
				.build();
	}
}
