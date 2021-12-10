/*
 * Copyright (c) 2021 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.integration.tests.security.project_installation;

import io.imunity.furms.api.project_installation.ProjectInstallationsService;
import io.imunity.furms.domain.policy_documents.PolicyAcceptance;
import io.imunity.furms.integration.tests.security.SecurityTestsBase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static io.imunity.furms.integration.tests.security.SecurityTestRulesValidator.forMethods;
import static io.imunity.furms.integration.tests.tools.users.TestUsersProvider.basicUser;
import static io.imunity.furms.integration.tests.tools.users.TestUsersProvider.communityAdmin;
import static io.imunity.furms.integration.tests.tools.users.TestUsersProvider.fenixAdmin;
import static io.imunity.furms.integration.tests.tools.users.TestUsersProvider.projectAdmin;
import static io.imunity.furms.integration.tests.tools.users.TestUsersProvider.projectUser;
import static io.imunity.furms.integration.tests.tools.users.TestUsersProvider.siteAdmin;
import static io.imunity.furms.integration.tests.tools.users.TestUsersProvider.siteSupport;

class ProjectInstallationsServiceSecurityTest extends SecurityTestsBase {

	@Autowired
	private ProjectInstallationsService service;

	@Test
	void shouldAllPublicMethodsHaveSecurityAnnotation() {
		assertThatAllInterfaceMethodsHaveBeenAnnotatedWithSecurityAnnotation(ProjectInstallationsService.class, service);
	}

	@Test
	void shouldPassForSecurityRulesForMethodsInProjectInstallationsService() {
		forMethods(
				() -> service.findAllSiteInstalledProjectsOfCurrentUser(),
				() -> service.findAllSiteInstalledProjectsByProjectId(project))
				.accessFor(
						basicUser(),
						fenixAdmin(),
						siteAdmin(site),
						siteAdmin(otherSite),
						siteSupport(site),
						siteSupport(otherSite),
						communityAdmin(community),
						communityAdmin(otherCommunity),
						projectAdmin(community, project),
						projectAdmin(otherCommunity, otherProject),
						projectUser(community, project),
						projectUser(otherCommunity, otherProject))
				.validate(server);
		forMethods(
				() -> service.findAllByProjectId(project),
				() -> service.findAllUpdatesByProjectId(project))
				.accessFor(
						communityAdmin(community),
						projectAdmin(community, project),
						projectUser(community, project))
				.deniedFor(
						basicUser(),
						fenixAdmin(),
						siteAdmin(site),
						siteAdmin(otherSite),
						siteSupport(site),
						siteSupport(otherSite),
						communityAdmin(otherCommunity),
						projectAdmin(otherCommunity, otherProject),
						projectUser(otherCommunity, otherProject))
				.validate(server);
		forMethods(
				() -> service.findAllByCommunityId(community),
				() -> service.findAllUpdatesByCommunityId(community))
				.accessFor(
						fenixAdmin(),
						communityAdmin(community))
				.deniedFor(
						basicUser(),
						siteAdmin(site),
						siteAdmin(otherSite),
						siteSupport(site),
						siteSupport(otherSite),
						communityAdmin(otherCommunity),
						projectAdmin(community, project),
						projectAdmin(otherCommunity, otherProject),
						projectUser(community, project),
						projectUser(otherCommunity, otherProject))
				.validate(server);
		forMethods(
				() -> service.findAllSiteInstalledProjectsBySiteId(site),
				() -> service.findAllBySiteId(site))
				.accessFor(
						fenixAdmin(),
						siteAdmin(site),
						siteSupport(site))
				.deniedFor(
						basicUser(),
						siteAdmin(otherSite),
						siteSupport(otherSite),
						communityAdmin(community),
						communityAdmin(otherCommunity),
						projectAdmin(community, project),
						projectAdmin(otherCommunity, otherProject),
						projectUser(community, project),
						projectUser(otherCommunity, otherProject))
				.validate(server);
	}

}
