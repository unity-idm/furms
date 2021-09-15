/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.unity.users;

import io.imunity.furms.domain.users.FURMSUser;
import io.imunity.furms.domain.users.PersistentId;
import io.imunity.furms.spi.users.FenixUsersDAO;
import io.imunity.furms.unity.client.users.UserService;
import org.springframework.stereotype.Component;

import java.util.List;

import static io.imunity.furms.domain.authz.roles.Role.FENIX_ADMIN;
import static io.imunity.furms.unity.common.UnityConst.FENIX_PATTERN;

@Component
class UnityFenixUsersDAO implements FenixUsersDAO {

	private final UserService userService;

	UnityFenixUsersDAO(UserService userService) {
		this.userService = userService;
	}

	@Override
	public List<FURMSUser> getAdminUsers() {
		return userService.getAllUsersByRole(FENIX_PATTERN, FENIX_ADMIN);
	}

	@Override
	public void addFenixAdminRole(PersistentId userId) {
		userService.addUserToGroup(userId, FENIX_PATTERN);
		userService.addUserRole(userId, FENIX_PATTERN, FENIX_ADMIN);
	}

	@Override
	public void removeFenixAdminRole(PersistentId userId) {
		userService.removeUserFromGroup(userId, FENIX_PATTERN);
	}
}
