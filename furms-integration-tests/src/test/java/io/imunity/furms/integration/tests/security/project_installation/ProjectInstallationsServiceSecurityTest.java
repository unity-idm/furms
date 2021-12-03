/*
 * Copyright (c) 2021 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.integration.tests.security.project_installation;

import io.imunity.furms.api.project_installation.ProjectInstallationsService;
import io.imunity.furms.integration.tests.security.SecurityTestsBase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class ProjectInstallationsServiceSecurityTest extends SecurityTestsBase {

	@Autowired
	private ProjectInstallationsService service;

	@Test
	void shouldAllPublicMethodsHaveSecurityAnnotation() {
		assertThatAllPublicMethodsBeAnnotatedWithSecurityAnnotation(service.getClass());
	}

	@Test
	void userWith_SITE_READ_canFindAllSiteInstalledProjectsBySiteId() throws Throwable {
		assertsForUserWith_SITE_READ(() -> service.findAllSiteInstalledProjectsBySiteId(site));
	}

	@Test
	void userWith_AUTHENTICATED_canFindAllSiteInstalledProjectsOfCurrentUser() throws Throwable {
		assertsForUserWith_AUTHENTICATED(() -> service.findAllSiteInstalledProjectsOfCurrentUser());
	}

	@Test
	void userWith_PROJECT_LIMITED_READ_canFindAllSiteInstalledProjectsByProjectId() throws Throwable {
		assertsForUserWith_PROJECT_LIMITED_READ(() -> service.findAllSiteInstalledProjectsByProjectId(project));
	}

	@Test
	void userWith_SITE_READ_canFindAllBySiteId() throws Throwable {
		assertsForUserWith_SITE_READ(() -> service.findAllBySiteId(site));
	}

	@Test
	void userWith_COMMUNITY_READ_canFindAllByCommunityId() throws Throwable {
		assertsForUserWith_COMMUNITY_READ(() -> service.findAllByCommunityId(community));
	}

	@Test
	void userWith_COMMUNITY_READ_canFindAllUpdatesByCommunityId() throws Throwable {
		assertsForUserWith_COMMUNITY_READ(() -> service.findAllUpdatesByCommunityId(community));
	}

	@Test
	void userWith_PROJECT_READ_canFindAllByProjectId() throws Throwable {
		assertsForUserWith_PROJECT_READ(() -> service.findAllByProjectId(project));
	}

	@Test
	void userWith_PROJECT_READ_canFindAllUpdatesByProjectId() throws Throwable {
		assertsForUserWith_PROJECT_READ(() -> service.findAllUpdatesByProjectId(project));
	}

}
