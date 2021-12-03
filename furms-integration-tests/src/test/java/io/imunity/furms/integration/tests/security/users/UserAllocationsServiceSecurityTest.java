/*
 * Copyright (c) 2021 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.integration.tests.security.users;

import io.imunity.furms.api.users.UserAllocationsService;
import io.imunity.furms.domain.users.FenixUserId;
import io.imunity.furms.domain.users.PersistentId;
import io.imunity.furms.integration.tests.security.SecurityTestsBase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class UserAllocationsServiceSecurityTest extends SecurityTestsBase {

	@Autowired
	private UserAllocationsService service;

	@Test
	void shouldAllPublicMethodsHaveSecurityAnnotation() {
		assertThatAllPublicMethodsBeAnnotatedWithSecurityAnnotation(service.getClass());
	}

	@Test
	void userWith_AUTHENTICATED_canFindCurrentUserSitesInstallations() throws Throwable {
		assertsForUserWith_AUTHENTICATED(() -> service.findCurrentUserSitesInstallations());
	}

	@Test
	void userWith_USERS_MAINTENANCE_canFindUserSitesInstallations() throws Throwable {
		assertsForUserWith_USERS_MAINTENANCE(() -> service.findUserSitesInstallations(new PersistentId("id")));
	}

	@Test
	void userWith_USERS_MAINTENANCE_canFindAllByFenixUserId() throws Throwable {
		assertsForUserWith_USERS_MAINTENANCE(() -> service.findAllByFenixUserId(new FenixUserId("id")));
	}

	@Test
	void userWith_SITE_READ_canFindAllBySiteId() throws Throwable {
		assertsForUserWith_SITE_READ(() -> service.findAllBySiteId(site));
	}

	@Test
	void userWith_PROJECT_READ_canFindAllByProjectId() throws Throwable {
		assertsForUserWith_PROJECT_READ(() -> service.findAllByProjectId(project));
	}
}
