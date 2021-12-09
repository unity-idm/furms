/*
 * Copyright (c) 2021 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.integration.tests.security.invitations;

import io.imunity.furms.api.invitations.InviteeService;
import io.imunity.furms.domain.invitations.InvitationId;
import io.imunity.furms.integration.tests.security.SecurityTestsBase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.UUID;

class InviteeServiceSecurityTest extends SecurityTestsBase {

	@Autowired
	private InviteeService service;

	@Test
	void shouldAllPublicMethodsHaveSecurityAnnotation() {
		assertThatAllInterfaceMethodsHaveBeenAnnotatedWithSecurityAnnotation(InviteeService.class, service);
	}

	@Test
	void userWith_AUTHENTICATED_canAcceptBy() throws Throwable {
		assertsForUserWith_AUTHENTICATED(() -> service.acceptBy(new InvitationId(UUID.randomUUID().toString())));
	}

	@Test
	void userWith_AUTHENTICATED_canFindAllByCurrentUser() throws Throwable {
		assertsForUserWith_AUTHENTICATED(() -> service.findAllByCurrentUser());
	}

	@Test
	void userWithoutCapabilitiesCanAcceptInvitationByRegistration() throws Throwable {
		assertThatThisMethodCanBeCalledWithoutCapabilities(() -> service.acceptInvitationByRegistration("registrationId"));
	}

}