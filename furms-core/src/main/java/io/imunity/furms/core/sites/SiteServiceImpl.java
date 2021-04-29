/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.core.sites;

import io.imunity.furms.api.authz.AuthzService;
import io.imunity.furms.api.sites.SiteService;
import io.imunity.furms.core.config.security.method.FurmsAuthorize;
import io.imunity.furms.core.utils.ExternalIdGenerator;
import io.imunity.furms.domain.authz.roles.ResourceId;
import io.imunity.furms.domain.authz.roles.Role;
import io.imunity.furms.domain.site_agent.PendingJob;
import io.imunity.furms.domain.site_agent.SiteAgentStatus;
import io.imunity.furms.domain.sites.*;
import io.imunity.furms.domain.users.FURMSUser;
import io.imunity.furms.domain.users.InviteUserEvent;
import io.imunity.furms.domain.users.PersistentId;
import io.imunity.furms.domain.users.RemoveUserRoleEvent;
import io.imunity.furms.site.api.SiteExternalIdsResolver;
import io.imunity.furms.site.api.site_agent.SiteAgentService;
import io.imunity.furms.site.api.site_agent.SiteAgentStatusService;
import io.imunity.furms.spi.sites.SiteRepository;
import io.imunity.furms.spi.sites.SiteWebClient;
import io.imunity.furms.spi.users.UsersDAO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static io.imunity.furms.domain.authz.roles.Capability.SITE_READ;
import static io.imunity.furms.domain.authz.roles.Capability.SITE_WRITE;
import static io.imunity.furms.domain.authz.roles.ResourceType.SITE;
import static io.imunity.furms.utils.ValidationUtils.assertFalse;
import static io.imunity.furms.utils.ValidationUtils.assertTrue;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toSet;
import static org.springframework.util.StringUtils.isEmpty;

@Service
class SiteServiceImpl implements SiteService, SiteExternalIdsResolver {

	private static final Logger LOG = LoggerFactory.getLogger(SiteServiceImpl.class);

	private final SiteRepository siteRepository;
	private final SiteServiceValidator validator;
	private final SiteWebClient webClient;
	private final UsersDAO usersDAO;
	private final ApplicationEventPublisher publisher;
	private final AuthzService authzService;
	private final SiteAgentService siteAgentService;
	private final SiteAgentStatusService siteAgentStatusService;

	SiteServiceImpl(SiteRepository siteRepository,
	                SiteServiceValidator validator,
	                SiteWebClient webClient,
	                UsersDAO usersDAO,
	                ApplicationEventPublisher publisher,
	                AuthzService authzService,
	                SiteAgentService siteAgentService,
	                SiteAgentStatusService siteAgentStatusService) {
		this.siteRepository = siteRepository;
		this.validator = validator;
		this.webClient = webClient;
		this.usersDAO = usersDAO;
		this.authzService = authzService;
		this.publisher = publisher;
		this.siteAgentService = siteAgentService;
		this.siteAgentStatusService = siteAgentStatusService;
	}

	@Override
	@FurmsAuthorize(capability = SITE_READ, resourceType = SITE, id = "id")
	public Optional<Site> findById(String id) {
		LOG.debug("Getting Site with id={}", id);
		return siteRepository.findById(id);
	}

	@Override
	@FurmsAuthorize(capability = SITE_READ, resourceType = SITE)
	public Set<Site> findAll() {
		LOG.debug("Getting all Sites");
		return siteRepository.findAll();
	}

	@Override
	@Transactional
	@FurmsAuthorize(capability = SITE_WRITE, resourceType = SITE)
	public void create(Site site) {
		validator.validateCreate(site);

		SiteExternalId externalId = new SiteExternalId(ExternalIdGenerator.generate(siteExternalId ->
			!siteRepository.existsByExternalId(new SiteExternalId(siteExternalId)))
		);
		String siteId = siteRepository.create(site, externalId);
		Site createdSite = siteRepository.findById(siteId)
				.orElseThrow(() -> new IllegalStateException("Site has not been saved to DB correctly."));
		LOG.info("Created Site in repository: {}", createdSite);
		try {
			webClient.create(createdSite);
			siteAgentService.initializeSiteConnection(externalId);
			LOG.info("Initialized connection channel to site agent: {}", siteId);
			publisher.publishEvent(new CreateSiteEvent(site.getId()));
			LOG.info("Created Site in Unity: {}", createdSite);
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

		Site oldSite = siteRepository.findById(site.getId())
				.orElseThrow(() -> new IllegalStateException("Site not found: " + site.getId()));

		String siteId = siteRepository.update(merge(oldSite, site));
		LOG.info("Updated Site in repository with ID={}, {}", siteId, site);
		Site updatedSite = siteRepository.findById(siteId)
				.orElseThrow(() -> new IllegalStateException("Site has not been saved to DB correctly."));
		try {
			webClient.update(updatedSite);
			publisher.publishEvent(new UpdateSiteEvent(updatedSite.getId()));
			LOG.info("Updated Site in Unity: {}", updatedSite);
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
		SiteExternalId externalId = siteRepository.findByIdExternalId(id);

		siteRepository.delete(id);
		LOG.info("Removed Site from repository with ID={}", id);
		try {
			webClient.delete(id);
			siteAgentService.removeSiteConnection(externalId);
			publisher.publishEvent(new RemoveSiteEvent(id));
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
	@FurmsAuthorize(capability = SITE_READ, resourceType = SITE, id="id")
	public List<FURMSUser> findAllAdmins(String id) {
		assertFalse(isEmpty(id), () -> new IllegalArgumentException("Could not get Site Administrators. Missing Site ID."));
		LOG.debug("Getting Site Administrators from Unity for Site ID={}", id);
		return webClient.getAllAdmins(id);
	}

	@Override
	@FurmsAuthorize(capability = SITE_WRITE, resourceType = SITE, id="siteId")
	public void inviteAdmin(String siteId, PersistentId userId) {
		assertNotEmpty(siteId, userId);
		Optional<FURMSUser> user = usersDAO.findById(userId);
		if (user.isEmpty()) {
			throw new IllegalArgumentException("Could not invite user due to wrong email adress.");
		}
		webClient.addAdmin(siteId, userId);
		publisher.publishEvent(new InviteUserEvent(user.get().id.orElse(null), new ResourceId(siteId, SITE)));
	}

	@Override
	@FurmsAuthorize(capability = SITE_WRITE, resourceType = SITE, id="siteId")
	public void addAdmin(String siteId, PersistentId userId) {
		assertNotEmpty(siteId, userId);

		try {
			webClient.addAdmin(siteId, userId);
			publisher.publishEvent(new InviteUserEvent(userId, new ResourceId(siteId, SITE)));
			LOG.info("Added Site Administrator ({}) in Unity for Site ID={}", userId, siteId);
		} catch (RuntimeException e) {
			LOG.error("Could not add Site Administrator: ", e);
			try {
				webClient.get(siteId).ifPresent(incompleteSite -> webClient.removeAdmin(siteId, userId));
			} catch (RuntimeException ex) {
				LOG.error("Could not add Site Administrator: Failed to rollback, problem during unity group deletion: ", ex);
			}
			throw e;
		}
	}

	@Override
	@FurmsAuthorize(capability = SITE_WRITE, resourceType = SITE, id="siteId")
	public void removeAdmin(String siteId, PersistentId userId) {
		assertNotEmpty(siteId, userId);

		try {
			webClient.removeAdmin(siteId, userId);
			publisher.publishEvent(new RemoveUserRoleEvent(userId, new ResourceId(siteId, SITE)));
			LOG.info("Removed Site Administrator ({}) from Unity for Site ID={}", userId, siteId);
		} catch (RuntimeException e) {
			LOG.error("Could not remove Site Administrator: ", e);
			throw e;
		}
	}

	@Override
	@FurmsAuthorize(capability = SITE_READ, resourceType = SITE, id="siteId")
	public boolean isAdmin(String siteId) {
		return authzService.isResourceMember(siteId, Role.SITE_ADMIN);
	}

	@Override
	@FurmsAuthorize(capability = SITE_READ, resourceType = SITE, id="siteId")
	public PendingJob<SiteAgentStatus> getSiteAgentStatus(String siteId) {
		SiteExternalId externalId = siteRepository.findByIdExternalId(siteId);
		return siteAgentStatusService.getStatus(externalId);
	}

	private Site merge(Site oldSite, Site site) {
		assertTrue(oldSite.getId().equals(site.getId()),() -> new IllegalArgumentException("There are different Sites during merge"));
		return Site.builder()
				.id(oldSite.getId())
				.name(site.getName())
				.logo(ofNullable(site.getLogo()).orElse(oldSite.getLogo()))
				.connectionInfo(ofNullable(site.getConnectionInfo()).orElse(oldSite.getConnectionInfo()))
				.sshKeyFromOptionMandatory(ofNullable(site.isSshKeyFromOptionMandatory()).orElse(oldSite.isSshKeyFromOptionMandatory()))
				.build();	
	}
	
	private void assertNotEmpty(String siteId, PersistentId userId) {
		assertFalse(isEmpty(siteId),
				() -> new IllegalArgumentException("Could not add Site Administrator. Missing Site ID"));
		assertFalse(isEmpty(userId),
				() -> new IllegalArgumentException("Could not add Site Administrator. Missing User ID"));

	}

	@Override
	public Set<SiteExternalId> findAllIds() {
		return siteRepository.findAll().stream()
			.map(Site::getExternalId)
			.collect(toSet());
	}
}
