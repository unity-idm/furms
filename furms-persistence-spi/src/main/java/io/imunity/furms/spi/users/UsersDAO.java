/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.spi.users;

import io.imunity.furms.domain.users.User;
import io.imunity.furms.domain.users.UserAttributes;
import io.imunity.furms.domain.users.UserStatus;

import java.util.List;
import java.util.Optional;


public interface UsersDAO {
	List<User> getAdminUsers();
	List<User> getAllUsers();
	Optional<User> findByEmail(String email);
	void addFenixAdminRole(String userId);
	void addProjectAdminRole(String communityId, String projectId, String userId);
	void removeFenixAdminRole(String userId);
	void setUserStatus(String fenixUserId, UserStatus status);
	UserStatus getUserStatus(String fenixUserId);
	Optional<User> findById(String userId);
	UserAttributes getUserAttributes(String fenixUserId);
}
