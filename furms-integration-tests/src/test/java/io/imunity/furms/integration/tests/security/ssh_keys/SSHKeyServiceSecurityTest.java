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

import static io.imunity.furms.integration.tests.security.SecurityTestRulesValidator.forMethods;
import static io.imunity.furms.integration.tests.tools.users.TestUsersProvider.basicUser;
import static io.imunity.furms.integration.tests.tools.users.TestUsersProvider.communityAdmin;
import static io.imunity.furms.integration.tests.tools.users.TestUsersProvider.fenixAdmin;
import static io.imunity.furms.integration.tests.tools.users.TestUsersProvider.projectAdmin;
import static io.imunity.furms.integration.tests.tools.users.TestUsersProvider.projectUser;
import static io.imunity.furms.integration.tests.tools.users.TestUsersProvider.siteAdmin;
import static io.imunity.furms.integration.tests.tools.users.TestUsersProvider.siteSupport;

class SSHKeyServiceSecurityTest extends SecurityTestsBase {

	@Autowired
	private SSHKeyService service;

	@Test
	void shouldAllPublicMethodsHaveSecurityAnnotation() {
		assertThatAllInterfaceMethodsHaveBeenAnnotatedWithSecurityAnnotation(SSHKeyService.class, service);
	}

	@Test
	void shouldPassForSecurityRulesForMethodsInSSHKeyService() {
		forMethods(
				() -> service.assertIsEligibleToManageKeys(),
				() -> service.findById(UUID.randomUUID().toString()),
				() -> service.findOwned(),
				() -> service.findByOwnerId(UUID.randomUUID().toString()),
				() -> service.findSiteSSHKeysByUserIdAndSite(persistentId, UUID.randomUUID().toString()),
				() -> service.create(SSHKey.builder().build()),
				() -> service.update(SSHKey.builder().build()),
				() -> service.delete(UUID.randomUUID().toString()),
				() -> service.isNamePresent("name"),
				() -> service.isNamePresentIgnoringRecord("name", UUID.randomUUID().toString()))
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
	}
}
