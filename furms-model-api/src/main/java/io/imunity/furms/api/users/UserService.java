/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.api.users;

import java.util.List;
import java.util.Optional;

import io.imunity.furms.domain.users.User;

public interface UserService {
	List<User> getAllUsers();
	List<User> getFenixAdmins();
	void invite(String email);
	void addFenixAdminRole(String userId);
	void removeFenixAdminRole(String userId);
	Optional<User> findById(String userId);
}
