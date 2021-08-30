/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.spi.users;

import io.imunity.furms.domain.users.*;

import java.util.List;
import java.util.Optional;


public interface UsersDAO {
	List<FURMSUser> getAdminUsers();
	List<FURMSUser> getAllUsers();
	void inviteFenixAdmin(String email);
	void addFenixAdminRole(PersistentId userId);
	void removeFenixAdminRole(PersistentId userId);
	void setUserStatus(FenixUserId fenixUserId, UserStatus status);
	UserStatus getUserStatus(FenixUserId fenixUserId);
	Optional<FURMSUser> findById(PersistentId userId);
	Optional<FURMSUser> findById(FenixUserId userId);
	UserAttributes getUserAttributes(FenixUserId fenixUserId);
	PersistentId getPersistentId(FenixUserId userId);
	FenixUserId getFenixUserId(PersistentId userId);
}
