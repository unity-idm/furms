/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.api.users;

import io.imunity.furms.domain.users.*;

import java.util.List;
import java.util.Optional;

public interface UserService {
	List<FURMSUser> getAllUsers();
	List<FURMSUser> getFenixAdmins();
	void inviteFenixAdmin(PersistentId userId);
	void addFenixAdminRole(PersistentId userId);
	void removeFenixAdminRole(PersistentId userId);
	void setUserStatus(FenixUserId fenixUserId, UserStatus status);
	UserStatus getUserStatus(FenixUserId fenixUserId);
	Optional<FURMSUser> findById(PersistentId userId);
	UserRecord getUserRecord(FenixUserId fenixUserId);
}
