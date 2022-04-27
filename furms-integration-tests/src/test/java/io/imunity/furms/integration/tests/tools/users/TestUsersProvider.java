/*
 * Copyright (c) 2021 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.integration.tests.tools.users;

import io.imunity.furms.domain.communities.CommunityId;
import io.imunity.furms.domain.projects.ProjectId;
import io.imunity.furms.domain.sites.SiteId;

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

	public static TestUser siteAdmin(SiteId siteId) {
		final TestUser testUser = basicUser();
		testUser.addSiteAdmin(siteId);
		return testUser;
	}

	public static TestUser siteSupport(SiteId siteId) {
		final TestUser testUser = basicUser();
		testUser.addSiteSupport(siteId);
		return testUser;
	}

	public static TestUser communityAdmin(CommunityId communityId) {
		final TestUser testUser = basicUser();
		testUser.addCommunityAdmin(communityId);
		return testUser;
	}

	public static TestUser projectAdmin(CommunityId communityId, ProjectId projectId) {
		final TestUser testUser = basicUser();
		testUser.addProjectAdmin(communityId, projectId);
		return testUser;
	}

	public static TestUser projectUser(CommunityId communityId, ProjectId projectId) {
		final TestUser testUser = basicUser();
		testUser.addProjectUser(communityId, projectId);
		return testUser;
	}
}
