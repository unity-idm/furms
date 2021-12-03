/*
 * Copyright (c) 2021 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.integration.tests.security.user_site_access;

import io.imunity.furms.api.user_site_access.UserSiteAccessService;
import io.imunity.furms.domain.users.FenixUserId;
import io.imunity.furms.integration.tests.security.SecurityTestsBase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class UserSiteAccessServiceSecurityTest extends SecurityTestsBase {

	@Autowired
	private UserSiteAccessService service;

	@Test
	void shouldAllPublicMethodsHaveSecurityAnnotation() {
		assertThatAllPublicMethodsBeAnnotatedWithSecurityAnnotation(service.getClass());
	}

	@Test
	void userWith_PROJECT_LIMITED_WRITE_canAddAccess() throws Throwable {
		assertsForUserWith_PROJECT_LIMITED_WRITE(() -> service.addAccess(site, project, new FenixUserId("id")));
	}

	@Test
	void userWith_PROJECT_LIMITED_WRITE_canRemoveAccess() throws Throwable {
		assertsForUserWith_PROJECT_LIMITED_WRITE(() -> service.removeAccess(site, project, new FenixUserId("id")));
	}

	@Test
	void userWith_PROJECT_READ_canGetUsersSitesAccesses() throws Throwable {
		assertsForUserWith_PROJECT_READ(() -> service.getUsersSitesAccesses(project));
	}

}
