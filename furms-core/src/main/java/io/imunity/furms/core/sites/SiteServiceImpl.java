/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.core.sites;

import io.imunity.furms.api.authz.AuthzService;
import io.imunity.furms.api.authz.CapabilityCollector;
import io.imunity.furms.api.sites.SiteService;
import io.imunity.furms.api.validation.exceptions.UserIsSiteAdmin;
import io.imunity.furms.api.validation.exceptions.UserIsSiteSupport;
import io.imunity.furms.api.validation.exceptions.UserWithoutFenixIdValidationError;
import io.imunity.furms.core.config.security.method.FurmsAuthorize;
import io.imunity.furms.core.invitations.InvitatoryService;
import io.imunity.furms.core.policy_documents.PolicyNotificationService;
import io.imunity.furms.core.post_commit.PostCommitRunner;
import io.imunity.furms.core.utils.ExternalIdGenerator;
import io.imunity.furms.domain.authz.roles.Capability;
import io.imunity.furms.domain.authz.roles.ResourceId;
import io.imunity.furms.domain.authz.roles.Role;
import io.imunity.furms.domain.invitations.Invitation;
import io.imunity.furms.domain.invitations.InvitationId;
import io.imunity.furms.domain.policy_documents.PolicyDocument;
import io.imunity.furms.domain.policy_documents.PolicyId;
import io.imunity.furms.domain.sites.Site;
import io.imunity.furms.domain.sites.SiteCreatedEvent;
import io.imunity.furms.domain.sites.SiteExternalId;
import io.imunity.furms.domain.sites.SiteId;
import io.imunity.furms.domain.sites.SiteRemovedEvent;
import io.imunity.furms.domain.sites.SiteUpdatedEvent;
import io.imunity.furms.domain.users.AllUsersAndSiteAdmins;
import io.imunity.furms.domain.users.FURMSUser;
import io.imunity.furms.domain.users.FenixUserId;
import io.imunity.furms.domain.users.PersistentId;
import io.imunity.furms.domain.users.UserAttribute;
import io.imunity.furms.domain.users.UserRoleGrantedEvent;
import io.imunity.furms.domain.users.UserRoleRevokedEvent;
import io.imunity.furms.site.api.SiteExternalIdsResolver;
import io.imunity.furms.site.api.site_agent.SiteAgentPolicyDocumentService;
import io.imunity.furms.site.api.site_agent.SiteAgentService;
import io.imunity.furms.spi.policy_docuemnts.PolicyDocumentRepository;
import io.imunity.furms.spi.sites.SiteGroupDAO;
import io.imunity.furms.spi.sites.SiteRepository;
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
import static io.imunity.furms.domain.authz.roles.ResourceType.SITE;
import static io.imunity.furms.utils.ValidationUtils.assertFalse;
import static java.util.stream.Collectors.toSet;

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
	private final SiteAgentPolicyDocumentService siteAgentPolicyDocumentService;
	private final CapabilityCollector capabilityCollector;
	private final PolicyNotificationService policyNotificationService;
	private final InvitatoryService invitatoryService;
	private final PostCommitRunner postCommitRunner;

	SiteServiceImpl(SiteRepository siteRepository,
	                SiteServiceValidator validator,
	                SiteGroupDAO webClient,
	                UsersDAO usersDAO,
	                ApplicationEventPublisher publisher,
	                AuthzService authzService,
	                SiteAgentService siteAgentService,
	                UserOperationRepository userOperationRepository,
	                PolicyDocumentRepository policyDocumentRepository,
	                SiteAgentPolicyDocumentService siteAgentPolicyDocumentService,
	                CapabilityCollector capabilityCollector,
	                PolicyNotificationService policyNotificationService,
	                InvitatoryService invitatoryService,
	                PostCommitRunner postCommitRunner) {
		this.siteRepository = siteRepository;
		this.validator = validator;
		this.webClient = webClient;
		this.usersDAO = usersDAO;
		this.authzService = authzService;
		this.publisher = publisher;
		this.siteAgentService = siteAgentService;
		this.userOperationRepository = userOperationRepository;
		this.policyDocumentRepository = policyDocumentRepository;
		this.siteAgentPolicyDocumentService = siteAgentPolicyDocumentService;
		this.capabilityCollector = capabilityCollector;
		this.policyNotificationService = policyNotificationService;
		this.invitatoryService = invitatoryService;
		this.postCommitRunner = postCommitRunner;
	}

	@Override
	@FurmsAuthorize(capability = AUTHENTICATED)
	public boolean existsById(SiteId id) {
		return siteRepository.exists(id);
	}

	@Override
	@FurmsAuthorize(capability = SITE_READ, resourceType = SITE, id = "id")
	public Optional<Site> findById(SiteId id) {
		LOG.debug("Getting Site with id={}", id);
		return siteRepository.findById(id);
	}

	@Override
	@FurmsAuthorize(capability = SITE_READ, resourceType = SITE, id = "ids", idCollections = true)
	public Set<Site> findAll(Set<SiteId> ids) {
		LOG.debug("Getting Site with ids={}", ids);
		return siteRepository.findAll(ids);
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
	@FurmsAuthorize(capability = AUTHENTICATED)
	public Set<Site> findUserSites(PersistentId userId) {
		LOG.debug("Getting all Sites for user");
		FenixUserId fenixUserId = usersDAO.getFenixUserId(userId);
		if (fenixUserId == null) {
			throw new UserWithoutFenixIdValidationError("User not logged via Fenix Central IdP");
		}
		return siteRepository.findAll().stream()
				.filter(site -> userOperationRepository.isUserAdded(site.getId(), fenixUserId))
				.collect(toSet());
	}

	@Override
	@FurmsAuthorize(capability = AUTHENTICATED)
	public Set<Site> findAllOfCurrentUserId() {
		final FURMSUser currentUser = authzService.getCurrentAuthNUser();
		return siteRepository.findAll().stream()
				.filter(site -> isBelongToSite(site, currentUser))
				.collect(toSet());
	}

	private boolean isBelongToSite(Site site, FURMSUser user) {
		final Set<Capability> capabilities = Set.of(SITE_READ, SITE_WRITE);
		return capabilityCollector.getCapabilities(user.roles, new ResourceId(site.getId(), SITE))
				.stream().anyMatch(capabilities::contains)
				|| (user.fenixUserId.isPresent() && userOperationRepository.isUserAdded(site.getId(), user.fenixUserId.get()));
	}

	@Override
	@Transactional
	@FurmsAuthorize(capability = SITE_WRITE, resourceType = SITE)
	public void create(Site site) {
		validator.validateCreate(site);

		SiteExternalId externalId = new SiteExternalId(ExternalIdGenerator.generate(siteExternalId ->
			!siteRepository.existsByExternalId(new SiteExternalId(siteExternalId)))
		);
		SiteId siteId = siteRepository.create(site, externalId);
		Site createdSite = siteRepository.findById(siteId)
				.orElseThrow(() -> new IllegalStateException("Site has not been saved to DB correctly."));
		LOG.info("Created Site in repository: {}", createdSite);
		try {
			webClient.create(createdSite);
			siteAgentService.initializeSiteConnection(externalId);
			LOG.info("Initialized connection channel to site agent: {}", siteId);
			publisher.publishEvent(new SiteCreatedEvent(createdSite));
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
	@FurmsAuthorize(capability = SITE_WRITE, resourceType = SITE, id = "siteId")
	public void updateName(SiteId siteId, String name) {
		Site oldSite = siteRepository.findById(siteId)
			.orElseThrow(() -> new IllegalStateException("Site not found: " + siteId));
		Site newSite = merge(oldSite, name);
		update(newSite);
	}

	@Override
	@Transactional
	@FurmsAuthorize(capability = SITE_WRITE, resourceType = SITE, id = "site.id")
	public void update(Site site) {
		validator.validateUpdate(site);
		Site oldSite = siteRepository.findById(site.getId())
				.orElseThrow(() -> new IllegalStateException("Site not found: " + site.getId()));
		SiteId siteId = siteRepository.update(site);
		LOG.info("Updated Site in repository with ID={}, {}", siteId, site);
		try {
			webClient.update(site);
			handlePolicyChange(site, oldSite);
			publisher.publishEvent(new SiteUpdatedEvent(oldSite, site));
			LOG.info("Updated Site in Unity: {}", site);
		} catch (RuntimeException e) {
			LOG.error("Could not update Site: ", e);
			throw e;
		}
	}

	private void handlePolicyChange(Site updatedSite, Site oldSite) {
		if(isPolicyChange(updatedSite, oldSite)) {
			postCommitRunner.runAfterCommit(() -> sendUpdateToSite(updatedSite, oldSite));
			if (updatedSite.getPolicyId() != null && updatedSite.getPolicyId().id != null) {
				policyNotificationService.notifyAllUsersAboutPolicyAssignmentChange(oldSite.getId());
			}
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
	public void delete(SiteId id) {
		validator.validateDelete(id);
		SiteExternalId externalId = siteRepository.findByIdExternalId(id);
		Site site = siteRepository.findById(id).get();
		siteRepository.delete(id);
		LOG.info("Removed Site from repository with ID={}", id);
		try {
			webClient.delete(id);
			siteAgentService.removeSiteConnection(externalId);
			publisher.publishEvent(new SiteRemovedEvent(site));
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
	@FurmsAuthorize(capability = SITE_READ, resourceType = SITE, id="siteId")
	public boolean isNamePresentIgnoringRecord(String name, SiteId siteId) {
		try {
			validator.validateIsNamePresentIgnoringRecord(name, siteId);
			return false;
		} catch (IllegalArgumentException e) {
			return true;
		}
	}

	@Override
	@FurmsAuthorize(capability = SITE_READ, resourceType = SITE)
	public AllUsersAndSiteAdmins findAllUsersAndSiteAdmins(SiteId id) {
		assertFalse(isEmpty(id), () -> new IllegalArgumentException("Could not get Site Administrators. Missing Site ID."));
		LOG.debug("Getting Site Administrators from Unity for Site ID={}", id);
		return webClient.getAllUsersAndSiteAdmins(id);
	}

	@Override
	@FurmsAuthorize(capability = SITE_READ, resourceType = SITE, id="id")
	public List<FURMSUser> findAllSiteUsers(SiteId id) {
		assertFalse(isEmpty(id), () -> new IllegalArgumentException("Could not get Site Supports. Missing Site ID."));
		LOG.debug("Getting Site Support from Unity for Site ID={}", id);
		return webClient.getSiteUsers(id, Set.of(Role.SITE_ADMIN, Role.SITE_SUPPORT));
	}

	@Override
	@FurmsAuthorize(capability = SITE_WRITE, resourceType = SITE, id="siteId")
	public void inviteAdmin(SiteId siteId, PersistentId userId) {
		ResourceId resourceId = new ResourceId(siteId, SITE);
		if(hasRole(userId, resourceId, Role.SITE_SUPPORT))
			throw new UserIsSiteSupport("User already has site support role");
		siteRepository.findById(siteId).ifPresent(site ->
			invitatoryService.inviteUser(userId, resourceId, Role.SITE_ADMIN, site.getName())
		);
	}

	private boolean hasRole(PersistentId userId, ResourceId resourceId, Role role) {
		return usersDAO.getUserAttributes(userId)
			.attributesByResource
			.getOrDefault(resourceId, Set.of()).contains(new UserAttribute(role));
	}

	private boolean hasRole(String email, ResourceId resourceId, Role role) {
		return usersDAO.getAllUsers().stream()
			.filter(user -> user.email.equals(email))
			.findAny()
			.filter(user -> user.id.isPresent())
			.flatMap(user -> user.id)
			.map(userId -> usersDAO.getUserAttributes(userId)
				.attributesByResource.getOrDefault(resourceId, Set.of())
				.contains(new UserAttribute(role)))
			.orElse(false);
	}

	@Override
	@FurmsAuthorize(capability = SITE_WRITE, resourceType = SITE, id="siteId")
	public void inviteAdmin(SiteId siteId, String email) {
		ResourceId resourceId = new ResourceId(siteId, SITE);
		if(hasRole(email, resourceId, Role.SITE_SUPPORT))
			throw new UserIsSiteSupport("User already has site support role");
		siteRepository.findById(siteId).ifPresent(site ->
			invitatoryService.inviteUser(email, new ResourceId(siteId, SITE), Role.SITE_ADMIN, site.getName())
		);
	}

	@Override
	@FurmsAuthorize(capability = SITE_WRITE, resourceType = SITE, id="siteId")
	public void inviteSupport(SiteId siteId, PersistentId userId) {
		ResourceId resourceId = new ResourceId(siteId, SITE);
		if(hasRole(userId, resourceId, Role.SITE_ADMIN))
			throw new UserIsSiteAdmin("User already has site admin role");
		siteRepository.findById(siteId).ifPresent(site ->
			invitatoryService.inviteUser(userId, resourceId, Role.SITE_SUPPORT, site.getName())
		);
	}

	@Override
	@FurmsAuthorize(capability = SITE_WRITE, resourceType = SITE, id="siteId")
	public void inviteSupport(SiteId siteId, String email) {
		ResourceId resourceId = new ResourceId(siteId, SITE);
		if(hasRole(email, resourceId, Role.SITE_ADMIN))
			throw new UserIsSiteAdmin("User already has site admin role");
		siteRepository.findById(siteId).ifPresent(site ->
			invitatoryService.inviteUser(email, new ResourceId(siteId, SITE), Role.SITE_SUPPORT, site.getName())
		);
	}

	@Override
	@FurmsAuthorize(capability = SITE_WRITE, resourceType = SITE, id="siteId")
	public Set<Invitation> findSiteAdminInvitations(SiteId siteId) {
		return invitatoryService.getInvitations(Role.SITE_ADMIN, siteId.id);
	}

	@Override
	@FurmsAuthorize(capability = SITE_WRITE, resourceType = SITE, id="siteId")
	public Set<Invitation> findSiteSupportInvitations(SiteId siteId) {
		return invitatoryService.getInvitations(Role.SITE_SUPPORT, siteId.id);
	}

	@Override
	@FurmsAuthorize(capability = SITE_WRITE, resourceType = SITE, id="siteId")
	public void resendInvitation(SiteId siteId, InvitationId invitationId) {
		if(!invitatoryService.checkAssociation(siteId, invitationId))
			throw new IllegalArgumentException(String.format("Invitation %s is not associated with this resource %s", siteId, invitationId));
		invitatoryService.resendInvitation(invitationId);
	}

	@Override
	@FurmsAuthorize(capability = SITE_WRITE, resourceType = SITE, id="siteId")
	public void changeInvitationRoleToSupport(SiteId siteId, InvitationId invitationId) {
		if(!invitatoryService.checkAssociation(siteId, invitationId))
			throw new IllegalArgumentException(String.format("Invitation %s is not associated with this resource %s", siteId, invitationId));
		invitatoryService.updateInvitationRole(invitationId, Role.SITE_SUPPORT);
	}

	@Override
	@FurmsAuthorize(capability = SITE_WRITE, resourceType = SITE, id="siteId")
	public void changeInvitationRoleToAdmin(SiteId siteId, InvitationId invitationId) {
		if(!invitatoryService.checkAssociation(siteId, invitationId))
			throw new IllegalArgumentException(String.format("Invitation %s is not associated with this resource %s", siteId, invitationId));
		invitatoryService.updateInvitationRole(invitationId, Role.SITE_ADMIN);
	}

	@Override
	@FurmsAuthorize(capability = SITE_WRITE, resourceType = SITE, id="siteId")
	public void removeInvitation(SiteId siteId, InvitationId invitationId) {
		if(!invitatoryService.checkAssociation(siteId, invitationId))
			throw new IllegalArgumentException(String.format("Invitation %s is not associate with this resource %s", siteId, invitationId));
		invitatoryService.removeInvitation(invitationId);
	}

	@Override
	@FurmsAuthorize(capability = SITE_WRITE, resourceType = SITE, id="siteId")
	public void addAdmin(SiteId siteId, PersistentId userId) {
		addUser(siteId, userId, () -> webClient.addSiteUser(siteId, userId, Role.SITE_ADMIN));
		String siteName = siteRepository.findById(siteId).get().getName();
		publisher.publishEvent(new UserRoleGrantedEvent(userId, new ResourceId(siteId, SITE), siteName,
			Role.SITE_ADMIN));
	}

	@Override
	@FurmsAuthorize(capability = SITE_WRITE, resourceType = SITE, id="siteId")
	public void addSupport(SiteId siteId, PersistentId userId) {
		addUser(siteId, userId, () -> webClient.addSiteUser(siteId, userId, Role.SITE_SUPPORT));
		String siteName = siteRepository.findById(siteId).get().getName();
		publisher.publishEvent(new UserRoleGrantedEvent(userId, new ResourceId(siteId, SITE), siteName,
			Role.SITE_SUPPORT));
	}

	@Override
	@FurmsAuthorize(capability = SITE_WRITE, resourceType = SITE, id="siteId")
	public void changeRoleToAdmin(SiteId siteId, PersistentId userId) {
		webClient.removeSiteUser(siteId, userId);
		addAdmin(siteId, userId);
	}

	@Override
	@FurmsAuthorize(capability = SITE_WRITE, resourceType = SITE, id="siteId")
	public void changeRoleToSupport(SiteId siteId, PersistentId userId) {
		webClient.removeSiteUser(siteId, userId);
		addSupport(siteId, userId);
	}

	private void addUser(SiteId siteId, PersistentId userId, Runnable adder) {
		assertNotEmpty(siteId, userId);
		try {
			adder.run();
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
	public void removeSiteUser(SiteId siteId, PersistentId userId) {
		assertNotEmpty(siteId, userId);
		Role role = authzService.isResourceMember(siteId, Role.SITE_SUPPORT) ? Role.SITE_SUPPORT : Role.SITE_ADMIN;
		String siteName = siteRepository.findById(siteId).get().getName();
		try {
			webClient.removeSiteUser(siteId, userId);
			publisher.publishEvent(new UserRoleRevokedEvent(userId, new ResourceId(siteId, SITE), siteName, role));
			LOG.info("Removed Site Administrator ({}) from Unity for Site ID={}", userId, siteId);
		} catch (RuntimeException e) {
			LOG.error("Could not remove Site Administrator: ", e);
			throw e;
		}
	}

	@Override
	@FurmsAuthorize(capability = SITE_READ, resourceType = SITE, id="siteId")
	public boolean isCurrentUserAdminOf(SiteId siteId) {
		return authzService.isResourceMember(siteId, Role.SITE_ADMIN);
	}

	@Override
	@FurmsAuthorize(capability = SITE_READ, resourceType = SITE, id="siteId")
	public boolean isCurrentUserSupportOf(SiteId siteId) {
		return authzService.isResourceMember(siteId, Role.SITE_SUPPORT);
	}

	private Site merge(Site oldSite, String name) {
		return Site.builder()
				.id(oldSite.getId())
				.name(name)
				.logo(oldSite.getLogo())
				.oauthClientId(oldSite.getOauthClientId())
				.connectionInfo(oldSite.getConnectionInfo())
				.sshKeyFromOptionMandatory(oldSite.isSshKeyFromOptionMandatory())
				.sshKeyHistoryLength(oldSite.getSshKeyHistoryLength())
				.policyId(oldSite.getPolicyId())
				.build();
	}
	
	private void assertNotEmpty(SiteId siteId, PersistentId userId) {
		assertFalse(isEmpty(siteId),
				() -> new IllegalArgumentException("Could not add Site Administrator. Missing Site ID"));
		assertFalse(isEmpty(siteId),
			() -> new IllegalArgumentException("Could not add Site Administrator. Missing Site ID"));
		assertFalse(isEmpty(userId),
				() -> new IllegalArgumentException("Could not add Site Administrator. Missing User ID"));

	}

	private boolean isEmpty(SiteId id) {
		return id == null || id.id == null;
	}

	private boolean isEmpty(PersistentId id) {
		return id == null || id.id == null;
	}
}
