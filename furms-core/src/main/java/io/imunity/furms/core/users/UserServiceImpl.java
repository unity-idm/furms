/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.core.users;

import static com.google.common.base.Preconditions.checkNotNull;
import static io.imunity.furms.domain.authz.roles.Capability.FENIX_ADMINS_MANAGEMENT;
import static io.imunity.furms.domain.authz.roles.Capability.READ_ALL_USERS;
import static io.imunity.furms.domain.authz.roles.Capability.USERS_MAINTENANCE;
import static io.imunity.furms.domain.authz.roles.ResourceType.APP_LEVEL;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import io.imunity.furms.api.users.UserService;
import io.imunity.furms.core.config.security.method.FurmsAuthorize;
import io.imunity.furms.domain.authz.roles.ResourceId;
import io.imunity.furms.domain.sites.Site;
import io.imunity.furms.domain.ssh_keys.SSHKey;
import io.imunity.furms.domain.users.CommunityMembership;
import io.imunity.furms.domain.users.FURMSUser;
import io.imunity.furms.domain.users.FenixUserId;
import io.imunity.furms.domain.users.InviteUserEvent;
import io.imunity.furms.domain.users.PersistentId;
import io.imunity.furms.domain.users.RemoveUserRoleEvent;
import io.imunity.furms.domain.users.SiteSSHKeys;
import io.imunity.furms.domain.users.UnknownUserException;
import io.imunity.furms.domain.users.UserAttribute;
import io.imunity.furms.domain.users.UserAttributes;
import io.imunity.furms.domain.users.UserRecord;
import io.imunity.furms.domain.users.UserStatus;
import io.imunity.furms.spi.exceptions.UnityFailureException;
import io.imunity.furms.spi.sites.SiteRepository;
import io.imunity.furms.spi.ssh_key_installation.InstalledSSHKeyRepository;
import io.imunity.furms.spi.ssh_keys.SSHKeyRepository;
import io.imunity.furms.spi.users.UsersDAO;

@Service
class UserServiceImpl implements UserService {
	private static final Logger LOG = LoggerFactory.getLogger(UserServiceImpl.class);
	private final UsersDAO usersDAO;
	private final SSHKeyRepository sshKeyRepository;
	private final SiteRepository siteRepository;
	private final InstalledSSHKeyRepository installedSSHKeyRepository;
	private final MembershipResolver membershipResolver;
	private final ApplicationEventPublisher publisher;


	public UserServiceImpl(UsersDAO usersDAO, MembershipResolver membershipResolver, ApplicationEventPublisher publisher,
						   SSHKeyRepository sshKeyRepository, SiteRepository siteRepository,
						   InstalledSSHKeyRepository installedSSHKeyRepository) {
		this.usersDAO = usersDAO;
		this.membershipResolver = membershipResolver;
		this.publisher = publisher;
		this.sshKeyRepository = sshKeyRepository;
		this.siteRepository = siteRepository;
		this.installedSSHKeyRepository = installedSSHKeyRepository;
	}

	@Override
	@FurmsAuthorize(capability = READ_ALL_USERS, resourceType = APP_LEVEL)
	public List<FURMSUser> getAllUsers(){
		return usersDAO.getAllUsers();
	}

	@Override
	@FurmsAuthorize(capability = FENIX_ADMINS_MANAGEMENT, resourceType = APP_LEVEL)
	public List<FURMSUser> getFenixAdmins(){
		return usersDAO.getAdminUsers();
	}

	@Override
	public void inviteFenixAdmin(PersistentId userId) {
		Optional<FURMSUser> user = usersDAO.findById(userId);
		if (user.isEmpty()) {
			throw new IllegalArgumentException("Could not invite user due to wrong email adress.");
		}
		usersDAO.addFenixAdminRole(userId);
		LOG.info("Adding FENIX admin role to {}", userId);
		publisher.publishEvent(new InviteUserEvent(userId, new ResourceId((String) null, APP_LEVEL)));
	}

	@Override
	@FurmsAuthorize(capability = FENIX_ADMINS_MANAGEMENT, resourceType = APP_LEVEL)
	public void addFenixAdminRole(PersistentId userId) {
		usersDAO.addFenixAdminRole(userId);
		LOG.info("Adding FENIX admin role to {}", userId);
		publisher.publishEvent(new InviteUserEvent(userId, new ResourceId((String) null, APP_LEVEL)));
	}

	@Override
	@FurmsAuthorize(capability = FENIX_ADMINS_MANAGEMENT, resourceType = APP_LEVEL)
	public void removeFenixAdminRole(PersistentId userId){
		LOG.info("Removing FENIX admin role from {}", userId);
		usersDAO.removeFenixAdminRole(userId);
		publisher.publishEvent(new RemoveUserRoleEvent(userId, new ResourceId((String) null, APP_LEVEL)));
	}

	@Override
	@FurmsAuthorize(capability = USERS_MAINTENANCE, resourceType = APP_LEVEL)
	public void setUserStatus(FenixUserId fenixUserId, UserStatus status) {
		checkNotNull(status);
		checkNotNull(fenixUserId);
		LOG.info("Setting {} status to {}", fenixUserId, status);
		try {
			usersDAO.setUserStatus(fenixUserId, status);
		} catch (UnityFailureException e) {
			LOG.info("Failed to resolve user", e);
			throw new UnknownUserException(fenixUserId);
		}
	}

	@Override
	@FurmsAuthorize(capability = USERS_MAINTENANCE, resourceType = APP_LEVEL)
	public UserStatus getUserStatus(FenixUserId fenixUserId) {
		checkNotNull(fenixUserId);
		try {
			return usersDAO.getUserStatus(fenixUserId);
		} catch (UnityFailureException e) {
			LOG.info("Failed to resolve user", e);
			throw new UnknownUserException(fenixUserId);
		}
	}

	@Override
	@FurmsAuthorize(capability = READ_ALL_USERS, resourceType = APP_LEVEL)
	public Optional<FURMSUser> findById(PersistentId userId) {
		checkNotNull(userId);
		checkNotNull(userId.id);
		return usersDAO.findById(userId);
	}

	@Override
	@FurmsAuthorize(capability = READ_ALL_USERS, resourceType = APP_LEVEL)
	public Optional<FURMSUser> findByFenixUserId(FenixUserId fenixUserId) {
		checkNotNull(fenixUserId);
		checkNotNull(fenixUserId.id);
		return usersDAO.findById(fenixUserId);
	}

	@Override
	@FurmsAuthorize(capability = USERS_MAINTENANCE, resourceType = APP_LEVEL)
	public UserRecord getUserRecord(FenixUserId fenixUserId) {
		checkNotNull(fenixUserId);
		try {
			UserAttributes userAttributes = usersDAO.getUserAttributes(fenixUserId);
			UserStatus userStatus = usersDAO.getUserStatus(fenixUserId);
			Set<CommunityMembership> communityMembership = membershipResolver
					.resolveCommunitiesMembership(userAttributes.attributesByResource);
			Set<UserAttribute> rootAttribtues = membershipResolver
					.filterExposedAttribtues(userAttributes.rootAttributes);
			PersistentId userId = usersDAO.getPersistentId(fenixUserId);
			Set<SiteSSHKeys> sshKeys = getSitesSSHKeys(userId);
			return new UserRecord(userStatus, rootAttribtues, communityMembership, sshKeys);
		} catch (UnityFailureException e) {
			LOG.info("Failed to resolve user", e);
			throw new UnknownUserException(fenixUserId);
		}
	}

	private Set<SiteSSHKeys> getSitesSSHKeys(PersistentId userId) {
		Map<String, Site> sites = siteRepository.findAll().stream()
				.collect(Collectors.toMap(s -> s.getId(), s -> s));
		Set<SSHKey> keys = sshKeyRepository.findAllByOwnerId(userId);
		Map<String, Set<String>> siteKeys = new HashMap<>();

		keys.forEach(key -> installedSSHKeyRepository.findBySSHKeyId(key.id).forEach(installedKey -> {

			if (siteKeys.get(installedKey.siteId) == null) {
				siteKeys.put(installedKey.siteId, new HashSet<>(Arrays.asList(installedKey.value)));
			} else {
				siteKeys.get(installedKey.siteId).add(installedKey.value);
			}

		}));

		return siteKeys.entrySet().stream()
				.filter(entry -> entry.getValue() != null && !entry.getValue().isEmpty())
				.map(entry -> new SiteSSHKeys(entry.getKey(), sites.get(entry.getKey()).getName(),
						entry.getValue()))
				.collect(Collectors.toSet());
	}
}





