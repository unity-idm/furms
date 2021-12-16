/*
 * Copyright (c) 2021 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.integration.tests.security.users;

import io.imunity.furms.api.users.FenixUserService;
import io.imunity.furms.domain.invitations.InvitationId;
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

class FenixUserServiceSecurityTest extends SecurityTestsBase {

	@Autowired
	private FenixUserService service;

	@Test
	void shouldAllPublicMethodsHaveSecurityAnnotation() {
		assertThatAllInterfaceMethodsHaveBeenAnnotatedWithSecurityAnnotation(FenixUserService.class, service);
	}

	@Test
	void shouldPassForSecurityRulesForMethodsInFenixUserService() {
		final InvitationId invitationId = new InvitationId(UUID.randomUUID().toString());
		forMethods(
				() -> service.getFenixAdmins(),
				() -> service.getFenixAdminsInvitations(),
				() -> service.inviteFenixAdmin("spock@uss-enterprise.gov"),
				() -> service.inviteFenixAdmin(persistentId),
				() -> service.resendFenixAdminInvitation(invitationId),
				() -> service.removeFenixAdminInvitation(invitationId),
				() -> service.removeFenixAdminRole(persistentId))
				.accessFor(
						fenixAdmin())
				.deniedFor(
						basicUser(),
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
				.verifySecurityRulesAndInterfaceCoverage(FenixUserService.class, server);
	}
}
