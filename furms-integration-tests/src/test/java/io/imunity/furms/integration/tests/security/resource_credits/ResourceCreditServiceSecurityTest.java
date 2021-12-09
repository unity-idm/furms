/*
 * Copyright (c) 2021 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.integration.tests.security.resource_credits;

import io.imunity.furms.api.resource_credits.ResourceCreditService;
import io.imunity.furms.domain.resource_credits.ResourceCredit;
import io.imunity.furms.integration.tests.security.SecurityTestsBase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class ResourceCreditServiceSecurityTest extends SecurityTestsBase {

	@Autowired
	private ResourceCreditService service;

	@Test
	void shouldAllPublicMethodsHaveSecurityAnnotation() {
		assertThatAllInterfaceMethodsHaveBeenAnnotatedWithSecurityAnnotation(ResourceCreditService.class, service);
	}

	@Test
	void userWith_SITE_READ_canFindWithAllocationsByIdAndSiteId() throws Throwable {
		assertsForUserWith_SITE_READ(() -> service.findWithAllocationsByIdAndSiteId(resourceCredit, site));
	}

	@Test
	void userWith_SITE_READ_canFindAllWithAllocations() throws Throwable {
		assertsForUserWith_SITE_READ(() -> service.findAllWithAllocations(site));
	}

	@Test
	void userWithoutResourceSpecified_SITE_READ_canFindAllNotExpiredByResourceTypeId() throws Throwable {
		assertsForUserWith_SITE_READ_withoutResourceSpecified(() -> service.findAllNotExpiredByResourceTypeId(resourceType));
	}

	@Test
	void userWithoutSpecificResource_SITE_READ_canFindAllWithAllocations() throws Throwable {
		assertsForUserWith_SITE_READ_withoutResourceSpecified(() -> service.findAllWithAllocations("name", false, false));
	}

	@Test
	void userWith_SITE_WRITE_canCreate() throws Throwable {
		assertsForUserWith_SITE_WRITE(() -> service.create(ResourceCredit.builder().siteId(site).build()));
	}

	@Test
	void userWith_SITE_WRITE_canUpdate() throws Throwable {
		assertsForUserWith_SITE_WRITE(() -> service.update(ResourceCredit.builder().siteId(site).build()));
	}

	@Test
	void userWith_SITE_WRITE_canDelete() throws Throwable {
		assertsForUserWith_SITE_WRITE(() -> service.delete(resourceCredit, site));
	}

	@Test
	void userWith_SITE_WRITE_canCheckThatHasCommunityAllocations() throws Throwable {
		assertsForUserWith_SITE_WRITE(() -> service.hasCommunityAllocations(resourceCredit, site));
	}

	@Test
	void userWith_SITE_WRITE_canGetOccupiedNames() throws Throwable {
		assertsForUserWith_SITE_WRITE(() -> service.getOccupiedNames(site));
	}

}
