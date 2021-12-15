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

import static io.imunity.furms.integration.tests.security.SecurityTestRulesValidator.forMethods;
import static io.imunity.furms.integration.tests.tools.users.TestUsersProvider.basicUser;
import static io.imunity.furms.integration.tests.tools.users.TestUsersProvider.communityAdmin;
import static io.imunity.furms.integration.tests.tools.users.TestUsersProvider.fenixAdmin;
import static io.imunity.furms.integration.tests.tools.users.TestUsersProvider.projectAdmin;
import static io.imunity.furms.integration.tests.tools.users.TestUsersProvider.projectUser;
import static io.imunity.furms.integration.tests.tools.users.TestUsersProvider.siteAdmin;
import static io.imunity.furms.integration.tests.tools.users.TestUsersProvider.siteSupport;

class UserApiKeyServiceSecurityTest extends SecurityTestsBase {

	@Autowired
	private UserApiKeyService service;

	@Test
	void shouldAllPublicMethodsHaveSecurityAnnotation() {
		assertThatAllInterfaceMethodsHaveBeenAnnotatedWithSecurityAnnotation(UserApiKeyService.class, service);
	}

	@Test
	void shouldPassForSecurityRulesForMethodsInUserApiKeyService() {
		forMethods(
				() -> service.findByUserId(new PersistentId("id")),
				() -> service.save(new PersistentId("id"), UUID.randomUUID().toString()),
				() -> service.revoke(new PersistentId("id")))
				.accessFor(
						fenixAdmin(),
						siteAdmin(site),
						siteAdmin(otherSite),
						communityAdmin(community),
						communityAdmin(otherCommunity),
						projectAdmin(community, project),
						projectAdmin(otherCommunity, otherProject))
				.deniedFor(
						basicUser(),
						siteSupport(site),
						siteSupport(otherSite),
						projectUser(community, project),
						projectUser(otherCommunity, otherProject))
		.verifySecurityRulesAndInterfaceCoverage(UserApiKeyService.class, server);
	}

}
