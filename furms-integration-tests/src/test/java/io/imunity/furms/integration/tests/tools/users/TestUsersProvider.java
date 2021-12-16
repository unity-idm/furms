/*
 * Copyright (c) 2021 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.integration.tests.tools.users;

import java.util.UUID;

public class TestUsersProvider {

	public static TestUser basicUser() {
		return new TestUser(UUID.randomUUID().toString(), UUID.randomUUID().toString(), 2);
	}

	public static TestUser fenixAdmin() {
		final TestUser user = new TestUser(UUID.randomUUID().toString(), UUID.randomUUID().toString(), 1);
		user.addFenixAdminRole();
		return user;
	}

	public static TestUser siteAdmin(final String siteId) {
		final TestUser testUser = basicUser();
		testUser.addSiteAdmin(siteId);
		return testUser;
	}

	public static TestUser siteSupport(final String siteId) {
		final TestUser testUser = basicUser();
		testUser.addSiteSupport(siteId);
		return testUser;
	}

	public static TestUser communityAdmin(final String communityId) {
		final TestUser testUser = basicUser();
		testUser.addCommunityAdmin(communityId);
		return testUser;
	}

	public static TestUser projectAdmin(final String communityId, final String projectId) {
		final TestUser testUser = basicUser();
		testUser.addProjectAdmin(communityId, projectId);
		return testUser;
	}

	public static TestUser projectUser(final String communityId, final String projectId) {
		final TestUser testUser = basicUser();
		testUser.addProjectUser(communityId, projectId);
		return testUser;
	}
}
