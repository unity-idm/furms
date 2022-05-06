/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.api.users;

import io.imunity.furms.domain.users.AllUsersAndFenixAdmins;
import io.imunity.furms.domain.users.FURMSUser;
import io.imunity.furms.domain.users.FenixUserId;
import io.imunity.furms.domain.users.PersistentId;
import io.imunity.furms.domain.users.UserRecord;
import io.imunity.furms.domain.users.UserStatus;

import java.util.List;
import java.util.Optional;

public interface UserService {
	List<FURMSUser> getAllUsers();
	AllUsersAndFenixAdmins getAllUsersAndFenixAdmins();
	void setUserStatus(FenixUserId fenixUserId, UserStatus status);
	UserStatus getUserStatus(FenixUserId fenixUserId);
	Optional<FURMSUser> findById(PersistentId userId);
	Optional<FURMSUser> findByFenixUserId(FenixUserId fenixUserId);
	UserRecord getUserRecord(FenixUserId fenixUserId);
}
