/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.core.sites;

import io.imunity.furms.api.authz.AuthzService;
import io.imunity.furms.api.sites.SiteService;
import io.imunity.furms.api.validation.exceptions.UserWithoutFenixIdValidationError;
import io.imunity.furms.core.config.security.method.FurmsAuthorize;
import io.imunity.furms.core.utils.ExternalIdGenerator;
import io.imunity.furms.domain.authz.roles.ResourceId;
import io.imunity.furms.domain.authz.roles.Role;
import io.imunity.furms.domain.policy_documents.PolicyDocument;
import io.imunity.furms.domain.policy_documents.PolicyId;
import io.imunity.furms.domain.site_agent.PendingJob;
import io.imunity.furms.domain.site_agent.SiteAgentStatus;
import io.imunity.furms.domain.sites.CreateSiteEvent;
import io.imunity.furms.domain.sites.RemoveSiteEvent;
import io.imunity.furms.domain.sites.Site;
import io.imunity.furms.domain.sites.SiteExternalId;
import io.imunity.furms.domain.sites.UpdateSiteEvent;
import io.imunity.furms.domain.users.FURMSUser;
import io.imunity.furms.domain.users.FenixUserId;
import io.imunity.furms.domain.invitations.InviteUserEvent;
import io.imunity.furms.domain.users.PersistentId;
import io.imunity.furms.domain.users.RemoveUserRoleEvent;
import io.imunity.furms.site.api.SiteExternalIdsResolver;
import io.imunity.furms.site.api.site_agent.SiteAgentPolicyDocumentService;
import io.imunity.furms.site.api.site_agent.SiteAgentService;
import io.imunity.furms.site.api.site_agent.SiteAgentStatusService;
import io.imunity.furms.spi.policy_docuemnts.PolicyDocumentRepository;
import io.imunity.furms.spi.sites.SiteRepository;
import io.imunity.furms.spi.sites.SiteGroupDAO;
import io.imunity.furms.spi.user_operation.UserOperationRepository;
import io.imunity.furms.spi.users.UsersDAO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import static io.imunity.furms.domain.authz.roles.Capability.AUTHENTICATED;
import static io.imunity.furms.domain.authz.roles.Capability.SITE_READ;
import static io.imunity.furms.domain.authz.roles.Capability.SITE_WRITE;
import static io.imunity.furms.domain.authz.roles.ResourceType.APP_LEVEL;
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
	private final UserOperationRepository userOperationRepository;
	private final SiteServiceValidator validator;
	private final SiteGroupDAO webClient;
	private final UsersDAO usersDAO;
	private final ApplicationEventPublisher publisher;
	private final AuthzService authzService;
	private final SiteAgentService siteAgentService;
	private final PolicyDocumentRepository policyDocumentRepository;
	private final SiteAgentStatusService siteAgentStatusService;
	private final SiteAgentPolicyDocumentService siteAgentPolicyDocumentService;

	SiteServiceImpl(SiteRepository siteRepository,
	                SiteServiceValidator validator,
	                SiteGroupDAO webClient,
	                UsersDAO usersDAO,
	                ApplicationEventPublisher publisher,
	                AuthzService authzService,
	                SiteAgentService siteAgentService,
	                SiteAgentStatusService siteAgentStatusService,
	                UserOperationRepository userOperationRepository,
	                PolicyDocumentRepository policyDocumentRepository,
	                SiteAgentPolicyDocumentService siteAgentPolicyDocumentService) {
		this.siteRepository = siteRepository;
		this.validator = validator;
		this.webClient = webClient;
		this.usersDAO = usersDAO;
		this.authzService = authzService;
		this.publisher = publisher;
		this.siteAgentService = siteAgentService;
		this.siteAgentStatusService = siteAgentStatusService;
		this.userOperationRepository = userOperationRepository;
		this.policyDocumentRepository = policyDocumentRepository;
		this.siteAgentPolicyDocumentService = siteAgentPolicyDocumentService;
	}

	@Override
	@FurmsAuthorize(capability = AUTHENTICATED, resourceType = APP_LEVEL)
	public boolean existsById(String id) {
		return siteRepository.exists(id);
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
	public Set<SiteExternalId> findAllIds() {
		return siteRepository.findAll().stream()
				.map(Site::getExternalId)
				.collect(toSet());
	}

	@Override
	@FurmsAuthorize(capability = AUTHENTICATED, resourceType = APP_LEVEL)
	public Set<Site> findUserSites(PersistentId userId) {
		LOG.debug("Getting all Sites for user");
		FenixUserId fenixUserId = usersDAO.getFenixUserId(userId);
		if (fenixUserId == null) {
			throw new UserWithoutFenixIdValidationError("User not logged via Fenix Central IdP");
		}	
		return siteRepository.findAll().stream()
				.filter(site -> userOperationRepository.isUserAdded(site.getId(), fenixUserId.id))
				.collect(toSet());
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
			if(isPolicyChange(site, oldSite))
				sendUpdateToSite(site, oldSite);
			publisher.publishEvent(new UpdateSiteEvent(updatedSite.getId()));
			LOG.info("Updated Site in Unity: {}", updatedSite);
		} catch (RuntimeException e) {
			LOG.error("Could not update Site: ", e);
			throw e;
		}
	}

	private void sendUpdateToSite(Site site, Site oldSite) {
		int revision;
		PolicyDocument policyDocument;
		if(isPolicyDisengage(site, oldSite)){
			revision = -1;
			policyDocument = getPolicyDocument(oldSite.getPolicyId());
		}
		else {
			policyDocument = getPolicyDocument(site.getPolicyId());
			revision = policyDocument.revision;
		}

		siteAgentPolicyDocumentService.updatePolicyDocument(oldSite.getExternalId(), PolicyDocument.builder()
			.id(policyDocument.id)
			.name(policyDocument.name)
			.revision(revision)
			.build());
	}

	private PolicyDocument getPolicyDocument(PolicyId policyId) {
		return policyDocumentRepository.findById(policyId)
			.orElseThrow(() -> new IllegalArgumentException(String.format("Policy id %s doesn't exist", policyId)));
	}

	private boolean isPolicyChange(Site site, Site oldSite) {
		return !Objects.equals(oldSite.getPolicyId(), site.getPolicyId());
	}

	private boolean isPolicyDisengage(Site site, Site oldSite) {
		return (oldSite.getPolicyId() != null && oldSite.getPolicyId().id != null) &&
			(site.getPolicyId() == null || site.getPolicyId().id == null);
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
	public List<FURMSUser> findAllAdministrators(String id) {
		assertFalse(isEmpty(id), () -> new IllegalArgumentException("Could not get Site Administrators. Missing Site ID."));
		LOG.debug("Getting Site Administrators from Unity for Site ID={}", id);
		return webClient.getAllSiteUsers(id, Set.of(Role.SITE_ADMIN));
	}

	@Override
	@FurmsAuthorize(capability = SITE_READ, resourceType = SITE, id="id")
	public List<FURMSUser> findAllSupportUsers(String id) {
		assertFalse(isEmpty(id), () -> new IllegalArgumentException("Could not get Site Supports. Missing Site ID."));
		LOG.debug("Getting Site Support from Unity for Site ID={}", id);
		return webClient.getAllSiteUsers(id, Set.of(Role.SITE_SUPPORT));
	}

	@Override
	@FurmsAuthorize(capability = SITE_READ, resourceType = SITE, id="id")
	public List<FURMSUser> findAllSiteUsers(String id) {
		assertFalse(isEmpty(id), () -> new IllegalArgumentException("Could not get Site Supports. Missing Site ID."));
		LOG.debug("Getting Site Support from Unity for Site ID={}", id);
		return webClient.getAllSiteUsers(id, Set.of(Role.SITE_ADMIN, Role.SITE_SUPPORT));
	}

	@Override
	@FurmsAuthorize(capability = SITE_WRITE, resourceType = SITE, id="siteId")
	public void inviteAdmin(String siteId, PersistentId userId) {
		inviteUser(siteId, userId, () -> webClient.addSiteUser(siteId, userId, Role.SITE_ADMIN));
	}

	@Override
	@FurmsAuthorize(capability = SITE_WRITE, resourceType = SITE, id="siteId")
	public void inviteSupport(String siteId, PersistentId userId) {
		inviteUser(siteId, userId, () -> webClient.addSiteUser(siteId, userId, Role.SITE_SUPPORT));
	}

	private void inviteUser(String siteId, PersistentId userId, Runnable inviter) {
		assertNotEmpty(siteId, userId);
		Optional<FURMSUser> user = usersDAO.findById(userId);
		if (user.isEmpty()) {
			throw new IllegalArgumentException("Could not invite user due to wrong email adress.");
		}
		inviter.run();
		publisher.publishEvent(new InviteUserEvent(user.get().id.orElse(null), new ResourceId(siteId, SITE)));
	}

	@Override
	@FurmsAuthorize(capability = SITE_WRITE, resourceType = SITE, id="siteId")
	public void addAdmin(String siteId, PersistentId userId) {
		addUser(siteId, userId, () -> webClient.addSiteUser(siteId, userId, Role.SITE_ADMIN));
	}

	@Override
	@FurmsAuthorize(capability = SITE_WRITE, resourceType = SITE, id="siteId")
	public void addSupport(String siteId, PersistentId userId) {
		addUser(siteId, userId, () -> webClient.addSiteUser(siteId, userId, Role.SITE_SUPPORT));
	}

	private void addUser(String siteId, PersistentId userId, Runnable adder) {
		assertNotEmpty(siteId, userId);

		try {
			adder.run();
			publisher.publishEvent(new InviteUserEvent(userId, new ResourceId(siteId, SITE)));
			LOG.info("Added Site Administrator ({}) in Unity for Site ID={}", userId, siteId);
		} catch (RuntimeException e) {
			LOG.error("Could not add Site Administrator: ", e);
			try {
				webClient.get(siteId).ifPresent(incompleteSite -> webClient.removeSiteUser(siteId, userId));
			} catch (RuntimeException ex) {
				LOG.error("Could not add Site Administrator: Failed to rollback, problem during unity group deletion: ", ex);
			}
			throw e;
		}
	}

	@Override
	@FurmsAuthorize(capability = SITE_WRITE, resourceType = SITE, id="siteId")
	public void removeSiteUser(String siteId, PersistentId userId) {
		assertNotEmpty(siteId, userId);

		try {
			webClient.removeSiteUser(siteId, userId);
			publisher.publishEvent(new RemoveUserRoleEvent(userId, new ResourceId(siteId, SITE)));
			LOG.info("Removed Site Administrator ({}) from Unity for Site ID={}", userId, siteId);
		} catch (RuntimeException e) {
			LOG.error("Could not remove Site Administrator: ", e);
			throw e;
		}
	}

	@Override
	@FurmsAuthorize(capability = SITE_READ, resourceType = SITE, id="siteId")
	public boolean isCurrentUserAdminOf(String siteId) {
		return authzService.isResourceMember(siteId, Role.SITE_ADMIN);
	}

	@Override
	@FurmsAuthorize(capability = SITE_READ, resourceType = SITE, id="siteId")
	public boolean isCurrentUserSupportOf(String siteId) {
		return authzService.isResourceMember(siteId, Role.SITE_SUPPORT);
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
				.oauthClientId(ofNullable(site.getOauthClientId()).orElse(oldSite.getOauthClientId()))
				.connectionInfo(ofNullable(site.getConnectionInfo()).orElse(oldSite.getConnectionInfo()))
				.sshKeyFromOptionMandatory(ofNullable(site.isSshKeyFromOptionMandatory()).orElse(oldSite.isSshKeyFromOptionMandatory()))
				.sshKeyHistoryLength(ofNullable(site.getSshKeyHistoryLength()).orElse(oldSite.getSshKeyHistoryLength()))
				.policyId(site.getPolicyId())
				.externalId(oldSite.getExternalId())
				.build();
	}
	
	private void assertNotEmpty(String siteId, PersistentId userId) {
		assertFalse(isEmpty(siteId),
				() -> new IllegalArgumentException("Could not add Site Administrator. Missing Site ID"));
		assertFalse(isEmpty(userId),
				() -> new IllegalArgumentException("Could not add Site Administrator. Missing User ID"));

	}
}
