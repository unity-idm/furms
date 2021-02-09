/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.core.users;

import static io.imunity.furms.domain.authz.roles.Capability.FENIX_ADMINS_MANAGEMENT;
import static io.imunity.furms.domain.authz.roles.Capability.READ_ALL_USERS;
import static io.imunity.furms.domain.authz.roles.Capability.USERS_MAINTENANCE;
import static io.imunity.furms.domain.authz.roles.ResourceType.APP_LEVEL;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import io.imunity.furms.api.users.UserService;
import io.imunity.furms.core.config.security.method.FurmsAuthorize;
import io.imunity.furms.domain.users.Attribute;
import io.imunity.furms.domain.users.CommunityMembership;
import io.imunity.furms.domain.users.User;
import io.imunity.furms.domain.users.UserAttributes;
import io.imunity.furms.domain.users.UserRecord;
import io.imunity.furms.domain.users.UserStatus;
import io.imunity.furms.spi.users.UsersDAO;

@Service
class UserServiceImpl implements UserService {
	private static final Logger LOG = LoggerFactory.getLogger(UserServiceImpl.class);
	private final UsersDAO usersDAO;
	private final MembershipResolver membershipResolver;


	public UserServiceImpl(UsersDAO usersDAO, MembershipResolver membershipResolver) {
		this.usersDAO = usersDAO;
		this.membershipResolver = membershipResolver;
	}

	@Override
	@FurmsAuthorize(capability = READ_ALL_USERS, resourceType = APP_LEVEL)
	public List<User> getAllUsers(){
		return usersDAO.getAllUsers();
	}

	@Override
	@FurmsAuthorize(capability = FENIX_ADMINS_MANAGEMENT, resourceType = APP_LEVEL)
	public List<User> getFenixAdmins(){
		return usersDAO.getAdminUsers();
	}

	@Override
	public void inviteFenixAdmin(String email) {
		Optional<User> user = usersDAO.findByEmail(email);
		if (user.isEmpty()) {
			throw new IllegalArgumentException("Could not invite user due to wrong email adress.");
		}
		addFenixAdminRole(user.get().id);
	}

	@Override
	@FurmsAuthorize(capability = FENIX_ADMINS_MANAGEMENT, resourceType = APP_LEVEL)
	public void addFenixAdminRole(String userId) {
		LOG.info("Adding FENIX admin role to {}", userId);
		usersDAO.addFenixAdminRole(userId);
	}

	@Override
	@FurmsAuthorize(capability = FENIX_ADMINS_MANAGEMENT, resourceType = APP_LEVEL)
	public void removeFenixAdminRole(String userId){
		LOG.info("Removing FENIX admin role from {}", userId);
		usersDAO.removeFenixAdminRole(userId);
	}

	@Override
	@FurmsAuthorize(capability = USERS_MAINTENANCE, resourceType = APP_LEVEL)
	public void setUserStatus(String fenixUserId, UserStatus status) {
		LOG.info("Setting {} status to {}", fenixUserId, status);
		usersDAO.setUserStatus(fenixUserId, status);
	}

	@Override
	@FurmsAuthorize(capability = USERS_MAINTENANCE, resourceType = APP_LEVEL)
	public UserStatus getUserStatus(String fenixUserId) {
		return usersDAO.getUserStatus(fenixUserId);
	}
	
	@Override
	@FurmsAuthorize(capability = READ_ALL_USERS, resourceType = APP_LEVEL)
	public Optional<User> findById(String userId) {
		return usersDAO.findById(userId);
	}

	@Override
	@FurmsAuthorize(capability = USERS_MAINTENANCE, resourceType = APP_LEVEL)
	public UserRecord getUserRecord(String fenixUserId) {
		UserAttributes userAttributes = usersDAO.getUserAttributes(fenixUserId);
		UserStatus userStatus = usersDAO.getUserStatus(fenixUserId);
		Set<CommunityMembership> communityMembership = 
				membershipResolver.resolveCommunitiesMembership(userAttributes.attributesByResource);
		Set<Attribute> rootAttribtues = membershipResolver.filterExposedAttribtues(userAttributes.rootAttributes);
		return new UserRecord(userStatus, rootAttribtues, communityMembership);
	}
}





