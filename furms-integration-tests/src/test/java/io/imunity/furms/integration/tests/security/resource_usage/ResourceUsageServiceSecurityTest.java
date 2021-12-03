/*
 * Copyright (c) 2021 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.integration.tests.security.resource_usage;

import io.imunity.furms.api.resource_usage.ResourceUsageService;
import io.imunity.furms.integration.tests.security.SecurityTestsBase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Set;

import static java.time.LocalDateTime.now;

class ResourceUsageServiceSecurityTest extends SecurityTestsBase {

	@Autowired
	private ResourceUsageService service;

	@Test
	void shouldAllPublicMethodsHaveSecurityAnnotation() {
		assertThatAllPublicMethodsBeAnnotatedWithSecurityAnnotation(service.getClass());
	}

	@Test
	void userWith_SITE_READ_canFindAllUserUsages() throws Throwable {
		assertsForUserWith_SITE_READ(() -> service.findAllUserUsages(site, Set.of(), now(), now()));
	}
}
