/*
 * Copyright (c) 2021 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.integration.tests.tools.users;

import io.imunity.furms.domain.authz.roles.Role;

import java.util.UUID;

public class TestUsersProvider {

	public static TestUser adminUser() {
		final TestUser user = new TestUser(UUID.randomUUID().toString(), UUID.randomUUID().toString(), 1);
		user.addRole("/", Role.FENIX_ADMIN);
		user.addRole("/fenix/users", Role.FENIX_ADMIN);
		return user;
	}

	public static TestUser basicUser() {
		return new TestUser(UUID.randomUUID().toString(), UUID.randomUUID().toString(), 2);
	}
}
