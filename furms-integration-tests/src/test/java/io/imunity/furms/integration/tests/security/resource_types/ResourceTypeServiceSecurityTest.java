/*
 * Copyright (c) 2021 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.integration.tests.security.resource_types;

import io.imunity.furms.api.resource_types.ResourceTypeService;
import io.imunity.furms.domain.resource_types.ResourceType;
import io.imunity.furms.integration.tests.security.SecurityTestsBase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class ResourceTypeServiceSecurityTest extends SecurityTestsBase {

	@Autowired
	private ResourceTypeService service;

	@Test
	void shouldAllPublicMethodsHaveSecurityAnnotation() {
		assertThatAllInterfaceMethodsHaveBeenAnnotatedWithSecurityAnnotation(ResourceTypeService.class, service);
	}

	@Test
	void userWith_SITE_READ_canFindById() throws Throwable {
		assertsForUserWith_SITE_READ(() -> service.findById(resourceType, site));
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
		assertsForUserWith_SITE_WRITE(() -> service.create(ResourceType.builder().siteId(site).build()));
	}

	@Test
	void userWith_SITE_WRITE_canUpdate() throws Throwable {
		assertsForUserWith_SITE_WRITE(() -> service.update(ResourceType.builder().siteId(site).build()));
	}

	@Test
	void userWith_SITE_WRITE_canDelete() throws Throwable {
		assertsForUserWith_SITE_WRITE(() -> service.delete(resourceType, site));
	}
}
