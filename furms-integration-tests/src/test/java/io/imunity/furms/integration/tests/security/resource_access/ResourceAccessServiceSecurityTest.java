/*
 * Copyright (c) 2021 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.integration.tests.security.resource_access;

import io.imunity.furms.api.resource_access.ResourceAccessService;
import io.imunity.furms.domain.resource_access.GrantAccess;
import io.imunity.furms.integration.tests.security.SecurityTestsBase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class ResourceAccessServiceSecurityTest extends SecurityTestsBase {

	@Autowired
	private ResourceAccessService service;

	@Test
	void shouldAllPublicMethodsHaveSecurityAnnotation() {
		assertThatAllPublicMethodsBeAnnotatedWithSecurityAnnotation(service.getClass());
	}

	@Test
	void userWith_PROJECT_READ_canFindAddedUser() throws Throwable {
		assertsForUserWith_PROJECT_READ(() -> service.findAddedUser(project));
	}

	@Test
	void userWith_SITE_READ_canFindAddedUserBySiteId() throws Throwable {
		assertsForUserWith_SITE_READ(() -> service.findAddedUserBySiteId(site));
	}

	@Test
	void userWith_PROJECT_READ_canFindUsersGrants() throws Throwable {
		assertsForUserWith_PROJECT_READ(() -> service.findUsersGrants(project));
	}

	@Test
	void userWith_PROJECT_LIMITED_WRITE_canGrantAccess() throws Throwable {
		assertsForUserWith_PROJECT_LIMITED_WRITE(() -> service.grantAccess(GrantAccess.builder().projectId(project).build()));
	}

	@Test
	void userWith_PROJECT_LIMITED_WRITE_canRevokeAccess() throws Throwable {
		assertsForUserWith_PROJECT_LIMITED_WRITE(() -> service.revokeAccess(GrantAccess.builder().projectId(project).build()));
	}



}
