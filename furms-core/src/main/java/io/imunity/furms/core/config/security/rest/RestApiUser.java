/*
 * Copyright (c) 2021 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.core.config.security.rest;

import io.imunity.furms.api.authz.FURMSUserProvider;
import io.imunity.furms.domain.users.FURMSUser;
import org.springframework.security.core.userdetails.User;

import java.util.Set;

class RestApiUser extends User implements FURMSUserProvider {

	private FURMSUser furmsUser;

	RestApiUser(String username, String password, FURMSUser furmsUser) {
		super(username, password, Set.of());
		this.furmsUser = furmsUser;
	}

	@Override
	public FURMSUser getFURMSUser() {
		return furmsUser;
	}

	@Override
	public void updateFURMSUser(FURMSUser furmsUser) {
		this.furmsUser = furmsUser;
	}
}
