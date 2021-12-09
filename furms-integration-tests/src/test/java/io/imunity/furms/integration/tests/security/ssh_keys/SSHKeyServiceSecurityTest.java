/*
 * Copyright (c) 2021 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.integration.tests.security.ssh_keys;

import io.imunity.furms.api.ssh_keys.SSHKeyService;
import io.imunity.furms.domain.ssh_keys.SSHKey;
import io.imunity.furms.domain.users.PersistentId;
import io.imunity.furms.integration.tests.security.SecurityTestsBase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.UUID;

class SSHKeyServiceSecurityTest extends SecurityTestsBase {

	@Autowired
	private SSHKeyService service;

	@Test
	void shouldAllPublicMethodsHaveSecurityAnnotation() {
		assertThatAllInterfaceMethodsHaveBeenAnnotatedWithSecurityAnnotation(SSHKeyService.class, service);
	}

	@Test
	void userWithoutResourceSpecified_OWNED_SSH_KEY_MANAGMENT_canAssertIsEligibleToManageKeys() throws Throwable {
		assertsForUserWith_OWNED_SSH_KEY_MANAGMENT_withoutResourceSpecified(
				() -> service.assertIsEligibleToManageKeys());
	}

	@Test
	void userWithoutResourceSpecified_OWNED_SSH_KEY_MANAGMENT_canFindById() throws Throwable {
		assertsForUserWith_OWNED_SSH_KEY_MANAGMENT_withoutResourceSpecified(
				() -> service.findById(UUID.randomUUID().toString()));
	}

	@Test
	void userWithoutResourceSpecified_OWNED_SSH_KEY_MANAGMENT_canFindOwned() throws Throwable {
		assertsForUserWith_OWNED_SSH_KEY_MANAGMENT_withoutResourceSpecified(
				() -> service.findOwned());
	}

	@Test
	void userWithoutResourceSpecified_OWNED_SSH_KEY_MANAGMENT_canFindByOwnerId() throws Throwable {
		assertsForUserWith_OWNED_SSH_KEY_MANAGMENT_withoutResourceSpecified(
				() -> service.findByOwnerId(UUID.randomUUID().toString()));
	}

	@Test
	void userWithoutResourceSpecified_OWNED_SSH_KEY_MANAGMENT_canFindSiteSSHKeysByUserIdAndSite() throws Throwable {
		assertsForUserWith_OWNED_SSH_KEY_MANAGMENT_withoutResourceSpecified(
				() -> service.findSiteSSHKeysByUserIdAndSite(new PersistentId("id"), UUID.randomUUID().toString()));
	}

	@Test
	void userWithoutResourceSpecified_OWNED_SSH_KEY_MANAGMENT_canCreate() throws Throwable {
		assertsForUserWith_OWNED_SSH_KEY_MANAGMENT_withoutResourceSpecified(
				() -> service.create(SSHKey.builder().build()));
	}

	@Test
	void userWithoutResourceSpecified_OWNED_SSH_KEY_MANAGMENT_canUpdate() throws Throwable {
		assertsForUserWith_OWNED_SSH_KEY_MANAGMENT_withoutResourceSpecified(
				() -> service.update(SSHKey.builder().build()));
	}

	@Test
	void userWithoutResourceSpecified_OWNED_SSH_KEY_MANAGMENT_canDelete() throws Throwable {
		assertsForUserWith_OWNED_SSH_KEY_MANAGMENT_withoutResourceSpecified(
				() -> service.delete(UUID.randomUUID().toString()));
	}

	@Test
	void userWithoutResourceSpecified_OWNED_SSH_KEY_MANAGMENT_canCheckIsNamePresent() throws Throwable {
		assertsForUserWith_OWNED_SSH_KEY_MANAGMENT_withoutResourceSpecified(
				() -> service.isNamePresent("name"));
	}

	@Test
	void userWithoutResourceSpecified_OWNED_SSH_KEY_MANAGMENT_canCheckIsNamePresentIgnoringRecord() throws Throwable {
		assertsForUserWith_OWNED_SSH_KEY_MANAGMENT_withoutResourceSpecified(
				() -> service.isNamePresentIgnoringRecord("name", UUID.randomUUID().toString()));
	}
}
