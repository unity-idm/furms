/*
 * Copyright (c) 2021 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.integration.tests.security.users;

import io.imunity.furms.api.users.FenixUserService;
import io.imunity.furms.domain.invitations.InvitationId;
import io.imunity.furms.domain.users.PersistentId;
import io.imunity.furms.integration.tests.security.SecurityTestsBase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.UUID;

class FenixUserServiceSecurityTest extends SecurityTestsBase {

	@Autowired
	private FenixUserService service;

	@Test
	void shouldAllPublicMethodsHaveSecurityAnnotation() {
		assertThatAllInterfaceMethodsHaveBeenAnnotatedWithSecurityAnnotation(FenixUserService.class, service);
	}

	@Test
	void userWith_FENIX_ADMINS_MANAGEMENT_canGetFenixAdmins() throws Throwable {
		assertsForUserWith_FENIX_ADMINS_MANAGEMENT(() -> service.getFenixAdmins());
	}

	@Test
	void userWith_FENIX_ADMINS_MANAGEMENT_canGetFenixAdminsInvitations() throws Throwable {
		assertsForUserWith_FENIX_ADMINS_MANAGEMENT(() -> service.getFenixAdminsInvitations());
	}

	@Test
	void userWith_FENIX_ADMINS_MANAGEMENT_canInviteFenixAdminByEmail() throws Throwable {
		assertsForUserWith_FENIX_ADMINS_MANAGEMENT(() -> service.inviteFenixAdmin("spock@uss-enterprise.gov"));
	}

	@Test
	void userWith_FENIX_ADMINS_MANAGEMENT_canInviteFenixAdminById() throws Throwable {
		assertsForUserWith_FENIX_ADMINS_MANAGEMENT(() -> service.inviteFenixAdmin(new PersistentId("id")));
	}

	@Test
	void userWith_FENIX_ADMINS_MANAGEMENT_canResendFenixAdminInvitation() throws Throwable {
		assertsForUserWith_FENIX_ADMINS_MANAGEMENT(() -> service.resendFenixAdminInvitation(new InvitationId(UUID.randomUUID().toString())));
	}

	@Test
	void userWith_FENIX_ADMINS_MANAGEMENT_canRemoveFenixAdminInvitation() throws Throwable {
		assertsForUserWith_FENIX_ADMINS_MANAGEMENT(() -> service.removeFenixAdminInvitation(new InvitationId(UUID.randomUUID().toString())));
	}

	@Test
	void userWith_FENIX_ADMINS_MANAGEMENT_canRemoveFenixAdminRole() throws Throwable {
		assertsForUserWith_FENIX_ADMINS_MANAGEMENT(() -> service.removeFenixAdminRole(new PersistentId("id")));
	}

}
