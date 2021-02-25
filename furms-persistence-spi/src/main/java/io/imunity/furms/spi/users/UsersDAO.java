/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.spi.users;

import io.imunity.furms.domain.users.FURMSUser;
import io.imunity.furms.domain.users.UserAttributes;
import io.imunity.furms.domain.users.UserStatus;

import java.util.List;
import java.util.Optional;


public interface UsersDAO {
	List<FURMSUser> getAdminUsers();
	List<FURMSUser> getAllUsers();
	void addFenixAdminRole(String userId);
	void removeFenixAdminRole(String userId);
	void setUserStatus(String fenixUserId, UserStatus status);
	UserStatus getUserStatus(String fenixUserId);
	Optional<FURMSUser> findById(String userId);
	UserAttributes getUserAttributes(String fenixUserId);
}
