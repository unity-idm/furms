/*
 * Copyright (c) 2021 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.integration.tests.security.user_site_access;

import io.imunity.furms.api.user_site_access.UserSiteAccessService;
import io.imunity.furms.domain.ssh_keys.SSHKey;
import io.imunity.furms.domain.users.FenixUserId;
import io.imunity.furms.integration.tests.security.SecurityTestsBase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.UUID;

import static io.imunity.furms.integration.tests.security.SecurityTestRulesValidator.forMethods;
import static io.imunity.furms.integration.tests.tools.users.TestUsersProvider.basicUser;
import static io.imunity.furms.integration.tests.tools.users.TestUsersProvider.communityAdmin;
import static io.imunity.furms.integration.tests.tools.users.TestUsersProvider.fenixAdmin;
import static io.imunity.furms.integration.tests.tools.users.TestUsersProvider.projectAdmin;
import static io.imunity.furms.integration.tests.tools.users.TestUsersProvider.projectUser;
import static io.imunity.furms.integration.tests.tools.users.TestUsersProvider.siteAdmin;
import static io.imunity.furms.integration.tests.tools.users.TestUsersProvider.siteSupport;

public class UserSiteAccessServiceSecurityTest extends SecurityTestsBase {

	@Autowired
	private UserSiteAccessService service;

	@Test
	void shouldAllPublicMethodsHaveSecurityAnnotation() {
		assertThatAllInterfaceMethodsHaveBeenAnnotatedWithSecurityAnnotation(UserSiteAccessService.class, service);
	}

	@Test
	void shouldPassForSecurityRulesForMethodsInUserSiteAccessService() {
		forMethods(
				() -> service.addAccess(site, project, fenixId),
				() -> service.removeAccess(site, project, fenixId))
				.accessFor(
						communityAdmin(community),
						projectAdmin(community, project))
				.deniedFor(
						basicUser(),
						fenixAdmin(),
						siteAdmin(site),
						siteAdmin(otherSite),
						siteSupport(site),
						siteSupport(otherSite),
						communityAdmin(otherCommunity),
						projectAdmin(otherCommunity, otherProject),
						projectUser(community, project),
						projectUser(otherCommunity, otherProject))
				.validate(server);
		forMethods(
				() -> service.getUsersSitesAccesses(project))
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
	}
}
