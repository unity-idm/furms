/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.core.users;

import static io.imunity.furms.domain.authz.roles.Capability.FENIX_ADMINS_MANAGEMENT;
import static io.imunity.furms.domain.authz.roles.Capability.READ_ALL_USERS;
import static io.imunity.furms.domain.authz.roles.ResourceType.APP_LEVEL;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import io.imunity.furms.api.users.UserService;
import io.imunity.furms.core.config.security.method.FurmsAuthorize;
import io.imunity.furms.domain.users.User;
import io.imunity.furms.spi.users.UsersDAO;

@Service
public class UserServiceImpl implements UserService {
	private final UsersDAO usersDAO;

	public UserServiceImpl(UsersDAO usersDAO) {
		this.usersDAO = usersDAO;
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
		usersDAO.addFenixAdminRole(userId);
	}

	@Override
	@FurmsAuthorize(capability = FENIX_ADMINS_MANAGEMENT, resourceType = APP_LEVEL)
	public void removeFenixAdminRole(String userId){
		usersDAO.removeFenixAdminRole(userId);
	}

	@Override
	@FurmsAuthorize(capability = READ_ALL_USERS, resourceType = APP_LEVEL)
	public Optional<User> findById(String userId) {
		return usersDAO.findById(userId);
	}
}