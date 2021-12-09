/*
 * Copyright (c) 2021 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.integration.tests.security.ssh_keys;

import io.imunity.furms.api.ssh_keys.SSHKeyOperationService;
import io.imunity.furms.integration.tests.security.SecurityTestsBase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.UUID;

class SSHKeyOperationServiceSecurityTest extends SecurityTestsBase {

	@Autowired
	private SSHKeyOperationService service;

	@Test
	void shouldAllPublicMethodsHaveSecurityAnnotation() {
		assertThatAllInterfaceMethodsHaveBeenAnnotatedWithSecurityAnnotation(SSHKeyOperationService.class, service);
	}

	@Test
	void userWithoutResourceSpecified_OWNED_SSH_KEY_MANAGMENT_canFindBySSHKeyIdAndSiteId() throws Throwable {
		assertsForUserWith_OWNED_SSH_KEY_MANAGMENT_withoutResourceSpecified(
				() -> service.findBySSHKeyIdAndSiteId(UUID.randomUUID().toString(), site));
	}

	@Test
	void userWithoutResourceSpecified_OWNED_SSH_KEY_MANAGMENT_canFindBySSHKeyId() throws Throwable {
		assertsForUserWith_OWNED_SSH_KEY_MANAGMENT_withoutResourceSpecified(
				() -> service.findBySSHKeyId(UUID.randomUUID().toString()));
	}
}
