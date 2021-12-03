/*
 * Copyright (c) 2021 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.integration.tests.security.user.api.key;

import io.imunity.furms.api.user.api.key.UserApiKeyService;
import io.imunity.furms.domain.users.PersistentId;
import io.imunity.furms.integration.tests.security.SecurityTestsBase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.UUID;

class UserApiKeyServiceSecurityTest extends SecurityTestsBase {

	@Autowired
	private UserApiKeyService service;

	@Test
	void shouldAllPublicMethodsHaveSecurityAnnotation() {
		assertThatAllPublicMethodsBeAnnotatedWithSecurityAnnotation(service.getClass());
	}

	@Test
	void userWithoutCapabilitiesCanFindUserByUserIdAndApiKey() throws Throwable {
		assertThatThisMethodCanBeCalledWithoutCapabilities(() ->
				service.findUserByUserIdAndApiKey(new PersistentId("id"), UUID.randomUUID()));
	}

	@Test
	void userWithoutResourceSpecified_REST_API_KEY_MANAGEMENT_canFindByUserId() throws Throwable {
		assertsForUserWith_REST_API_KEY_MANAGEMENT_withoutResourceSpecified(() -> service.findByUserId(new PersistentId("id")));
	}

	@Test
	void userWithoutResourceSpecified_REST_API_KEY_MANAGEMENT_canSave() throws Throwable {
		assertsForUserWith_REST_API_KEY_MANAGEMENT_withoutResourceSpecified(
				() -> service.save(new PersistentId("id"), UUID.randomUUID().toString()));
	}

	@Test
	void userWithoutResourceSpecified_REST_API_KEY_MANAGEMENT_canRevoke() throws Throwable {
		assertsForUserWith_REST_API_KEY_MANAGEMENT_withoutResourceSpecified(() -> service.revoke(new PersistentId("id")));
	}

}
