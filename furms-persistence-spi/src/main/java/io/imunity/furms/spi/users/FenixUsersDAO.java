/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.spi.users;

import io.imunity.furms.domain.users.FURMSUser;
import io.imunity.furms.domain.users.PersistentId;

import java.util.List;

public interface FenixUsersDAO {
	List<FURMSUser> getAdminUsers();
	void addFenixAdminRole(PersistentId userId);
	void removeFenixAdminRole(PersistentId userId);
}
