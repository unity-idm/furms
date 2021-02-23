/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.api.users;

import io.imunity.furms.domain.users.User;
import io.imunity.furms.domain.users.UserRecord;
import io.imunity.furms.domain.users.UserStatus;

import java.util.List;
import java.util.Optional;


public interface UserService {
	List<User> getAllUsers();
	List<User> getFenixAdmins();
	void inviteFenixAdmin(String userId);
	void addFenixAdminRole(String userId);
	void removeFenixAdminRole(String userId);
	void setUserStatus(String fenixUserId, UserStatus status);
	UserStatus getUserStatus(String fenixUserId);
	Optional<User> findById(String userId);
	UserRecord getUserRecord(String fenixUserId);
}
