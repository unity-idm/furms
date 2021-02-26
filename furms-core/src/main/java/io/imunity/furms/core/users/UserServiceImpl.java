/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.core.users;

import io.imunity.furms.api.users.UserService;
import io.imunity.furms.core.config.security.method.FurmsAuthorize;
import io.imunity.furms.domain.authz.roles.ResourceId;
import io.imunity.furms.domain.users.*;
import io.imunity.furms.spi.exceptions.UnityFailureException;
import io.imunity.furms.spi.users.UsersDAO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static com.google.common.base.Preconditions.checkNotNull;
import static io.imunity.furms.domain.authz.roles.Capability.*;
import static io.imunity.furms.domain.authz.roles.ResourceType.APP_LEVEL;

@Service
class UserServiceImpl implements UserService {
	private static final Logger LOG = LoggerFactory.getLogger(UserServiceImpl.class);
	private final UsersDAO usersDAO;
	private final MembershipResolver membershipResolver;
	private final ApplicationEventPublisher publisher;


	public UserServiceImpl(UsersDAO usersDAO, MembershipResolver membershipResolver, ApplicationEventPublisher publisher) {
		this.usersDAO = usersDAO;
		this.membershipResolver = membershipResolver;
		this.publisher = publisher;
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
		return usersDAO.findById(userId);
	}

	@Override
	@FurmsAuthorize(capability = USERS_MAINTENANCE, resourceType = APP_LEVEL)
	public UserRecord getUserRecord(FenixUserId fenixUserId) {
		checkNotNull(fenixUserId);
		try {
			UserAttributes userAttributes = usersDAO.getUserAttributes(fenixUserId);
			UserStatus userStatus = usersDAO.getUserStatus(fenixUserId);
			Set<CommunityMembership> communityMembership = 
					membershipResolver.resolveCommunitiesMembership(userAttributes.attributesByResource);
			Set<UserAttribute> rootAttribtues = membershipResolver.filterExposedAttribtues(userAttributes.rootAttributes);
			return new UserRecord(userStatus, rootAttribtues, communityMembership);
		} catch (UnityFailureException e) {
			LOG.info("Failed to resolve user", e);
			throw new UnknownUserException(fenixUserId);
		}
	}
}





