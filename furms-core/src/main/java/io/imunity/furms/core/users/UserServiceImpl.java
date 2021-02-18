/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.core.users;

import io.imunity.furms.api.events.CRUD;
import io.imunity.furms.api.events.FurmsEvent;
import io.imunity.furms.api.events.UserEvent;
import io.imunity.furms.api.users.UserService;
import io.imunity.furms.core.config.security.method.FurmsAuthorize;
import io.imunity.furms.domain.authz.roles.Role;
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
	public List<User> getAllUsers(){
		List<User> users = usersDAO.getAllUsers();
		publisher.publishEvent(new FurmsEvent<>(users, CRUD.READ));
		return users;
	}

	@Override
	@FurmsAuthorize(capability = FENIX_ADMINS_MANAGEMENT, resourceType = APP_LEVEL)
	public List<User> getFenixAdmins(){
		List<User> adminUsers = usersDAO.getAdminUsers();
		publisher.publishEvent(new FurmsEvent<>(new UserEvent(Role.FENIX_ADMIN, null), CRUD.READ));
		return adminUsers;
	}

	@Override
	public void inviteFenixAdmin(String email) {
		Optional<User> user = usersDAO.findByEmail(email);
		if (user.isEmpty()) {
			throw new IllegalArgumentException("Could not invite user due to wrong email adress.");
		}
		addFenixAdminRole(user.get().id);
		publisher.publishEvent(new FurmsEvent<>(new UserEvent(Role.FENIX_ADMIN, user.get().id), CRUD.CREATE));
	}

	@Override
	@FurmsAuthorize(capability = FENIX_ADMINS_MANAGEMENT, resourceType = APP_LEVEL)
	public void addFenixAdminRole(String userId) {
		LOG.info("Adding FENIX admin role to {}", userId);
		usersDAO.addFenixAdminRole(userId);
		publisher.publishEvent(new FurmsEvent<>(new UserEvent(Role.FENIX_ADMIN, userId), CRUD.CREATE));
	}

	@Override
	@FurmsAuthorize(capability = FENIX_ADMINS_MANAGEMENT, resourceType = APP_LEVEL)
	public void removeFenixAdminRole(String userId){
		LOG.info("Removing FENIX admin role from {}", userId);
		usersDAO.removeFenixAdminRole(userId);
		publisher.publishEvent(new FurmsEvent<>(new UserEvent(Role.FENIX_ADMIN, userId), CRUD.DELETE));
	}

	@Override
	@FurmsAuthorize(capability = USERS_MAINTENANCE, resourceType = APP_LEVEL)
	public void setUserStatus(String fenixUserId, UserStatus status) {
		checkNotNull(status);
		checkNotNull(fenixUserId);
		LOG.info("Setting {} status to {}", fenixUserId, status);
		try {
			usersDAO.setUserStatus(fenixUserId, status);
			publisher.publishEvent(new FurmsEvent<>(fenixUserId, CRUD.UPDATE));
		} catch (UnityFailureException e) {
			LOG.info("Failed to resolve user", e);
			throw new UnknownUserException(fenixUserId);
		}
	}

	@Override
	@FurmsAuthorize(capability = USERS_MAINTENANCE, resourceType = APP_LEVEL)
	public UserStatus getUserStatus(String fenixUserId) {
		checkNotNull(fenixUserId);
		try {
			UserStatus userStatus = usersDAO.getUserStatus(fenixUserId);
			publisher.publishEvent(new FurmsEvent<>(fenixUserId, CRUD.READ));
			return userStatus;
		} catch (UnityFailureException e) {
			LOG.info("Failed to resolve user", e);
			throw new UnknownUserException(fenixUserId);
		}
	}
	
	@Override
	@FurmsAuthorize(capability = READ_ALL_USERS, resourceType = APP_LEVEL)
	public Optional<User> findById(String userId) {
		Optional<User> user = usersDAO.findById(userId);
		publisher.publishEvent(new FurmsEvent<>(userId, CRUD.READ));
		return user;
	}

	@Override
	@FurmsAuthorize(capability = USERS_MAINTENANCE, resourceType = APP_LEVEL)
	public UserRecord getUserRecord(String fenixUserId) {
		checkNotNull(fenixUserId);
		try {
			UserAttributes userAttributes = usersDAO.getUserAttributes(fenixUserId);
			UserStatus userStatus = usersDAO.getUserStatus(fenixUserId);
			Set<CommunityMembership> communityMembership = 
					membershipResolver.resolveCommunitiesMembership(userAttributes.attributesByResource);
			Set<UserAttribute> rootAttribtues = membershipResolver.filterExposedAttribtues(userAttributes.rootAttributes);
			publisher.publishEvent(new FurmsEvent<>(fenixUserId, CRUD.READ));
			return new UserRecord(userStatus, rootAttribtues, communityMembership);
		} catch (UnityFailureException e) {
			LOG.info("Failed to resolve user", e);
			throw new UnknownUserException(fenixUserId);
		}
	}
}





