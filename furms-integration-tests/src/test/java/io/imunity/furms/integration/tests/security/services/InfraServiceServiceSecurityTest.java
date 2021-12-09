/*
 * Copyright (c) 2021 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.integration.tests.security.services;

import io.imunity.furms.api.services.InfraServiceService;
import io.imunity.furms.domain.services.InfraService;
import io.imunity.furms.integration.tests.security.SecurityTestsBase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class InfraServiceServiceSecurityTest extends SecurityTestsBase {

	@Autowired
	private InfraServiceService service;

	@Test
	void shouldAllPublicMethodsHaveSecurityAnnotation() {
		assertThatAllInterfaceMethodsHaveBeenAnnotatedWithSecurityAnnotation(InfraServiceService.class, service);
	}

	@Test
	void userWith_SITE_READ_canFindById() throws Throwable {
		assertsForUserWith_SITE_READ(() -> service.findById(infraService, site));
	}

	@Test
	void userWith_SITE_READ_canFindAllBySiteId() throws Throwable {
		assertsForUserWith_SITE_READ(() -> service.findAll(site));
	}

	@Test
	void userWithoutResourceSpecified_SITE_READ_canFindAll() throws Throwable {
		assertsForUserWith_SITE_READ_withoutResourceSpecified(() -> service.findAll());
	}

	@Test
	void userWith_SITE_WRITE_canCreate() throws Throwable {
		assertsForUserWith_SITE_WRITE(() -> service.create(InfraService.builder().siteId(site).build()));
	}

	@Test
	void userWith_SITE_WRITE_canUpdate() throws Throwable {
		assertsForUserWith_SITE_WRITE(() -> service.update(InfraService.builder().siteId(site).build()));
	}

	@Test
	void userWith_SITE_WRITE_canDelete() throws Throwable {
		assertsForUserWith_SITE_WRITE(() -> service.delete(infraService, site));
	}

}
