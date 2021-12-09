/*
 * Copyright (c) 2021 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.integration.tests.security.users;

import io.imunity.furms.api.users.UserService;
import io.imunity.furms.domain.users.FenixUserId;
import io.imunity.furms.domain.users.PersistentId;
import io.imunity.furms.domain.users.UserStatus;
import io.imunity.furms.integration.tests.security.SecurityTestsBase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class UserServiceSecurityTest extends SecurityTestsBase {

	@Autowired
	private UserService service;

	@Test
	void shouldAllPublicMethodsHaveSecurityAnnotation() {
		assertThatAllInterfaceMethodsHaveBeenAnnotatedWithSecurityAnnotation(UserService.class, service);
	}

	@Test
	void userWith_READ_ALL_USERS_canGetFenixAdmins() throws Throwable {
		assertsForUserWith_READ_ALL_USERS(() -> service.getAllUsers());
	}

	@Test
	void userWith_USERS_MAINTENANCE_canSetUserStatus() throws Throwable {
		assertsForUserWith_USERS_MAINTENANCE(() -> service.setUserStatus(new FenixUserId("id"), UserStatus.ENABLED));
	}

	@Test
	void userWith_USERS_MAINTENANCE_canGetUserStatus() throws Throwable {
		assertsForUserWith_USERS_MAINTENANCE(() -> service.getUserStatus(new FenixUserId("id")));
	}

	@Test
	void userWith_READ_ALL_USERS_canFindById() throws Throwable {
		assertsForUserWith_READ_ALL_USERS(() -> service.findById(new PersistentId("id")));
	}

	@Test
	void userWith_READ_ALL_USERS_canFindByFenixId() throws Throwable {
		assertsForUserWith_READ_ALL_USERS(() -> service.findByFenixUserId(new FenixUserId("id")));
	}

	@Test
	void userWith_USERS_MAINTENANCE_canGetUserRecord() throws Throwable {
		assertsForUserWith_USERS_MAINTENANCE(() -> service.getUserRecord(new FenixUserId("id")));
	}

}
