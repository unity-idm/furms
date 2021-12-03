/*
 * Copyright (c) 2021 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.integration.tests.security.community_allocation;

import io.imunity.furms.api.community_allocation.CommunityAllocationService;
import io.imunity.furms.domain.community_allocation.CommunityAllocation;
import io.imunity.furms.integration.tests.security.SecurityTestsBase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class CommunityAllocationServiceSecurityTest extends SecurityTestsBase {

	@Autowired
	private CommunityAllocationService service;

	@Test
	void shouldAllPublicMethodsHaveSecurityAnnotation() {
		assertThatAllPublicMethodsBeAnnotatedWithSecurityAnnotation(service.getClass());
	}

	@Test
	void userWithoutResourceSpecified_COMMUNITY_READ_canFindById() throws Throwable {
		assertsForUserWith_COMMUNITY_READ_withoutResourceSpecified(() -> service.findById(communityAllocation));
	}

	@Test
	void userWithoutResourceSpecified_COMMUNITY_READ_canFindByIdWithRelatedObjects() throws Throwable {
		assertsForUserWith_COMMUNITY_READ_withoutResourceSpecified(() -> service.findByIdWithRelatedObjects(communityAllocation));
	}

	@Test
	void userWith_COMMUNITY_READ_canFindByCommunityIdAndIdWithRelatedObjects() throws Throwable {
		assertsForUserWith_COMMUNITY_READ(() -> service.findByCommunityIdAndIdWithRelatedObjects(community, communityAllocation));
	}

	@Test
	void userWithoutResourceSpecified_COMMUNITY_READ_canFindAll() throws Throwable {
		assertsForUserWith_COMMUNITY_READ_withoutResourceSpecified(() -> service.findAll());
	}

	@Test
	void userWith_COMMUNITY_READ_canFindAllByCommunityId() throws Throwable {
		assertsForUserWith_COMMUNITY_READ(() -> service.findAllByCommunityId(community));
	}

	@Test
	void userWith_COMMUNITY_READ_canGetOccupiedNames() throws Throwable {
		assertsForUserWith_COMMUNITY_READ(() -> service.getOccupiedNames(community));
	}

	@Test
	void userWith_COMMUNITY_READ_canFindAllWithRelatedObjects() throws Throwable {
		assertsForUserWith_COMMUNITY_READ(() -> service.findAllWithRelatedObjects(community));
	}

	@Test
	void userWith_COMMUNITY_READ_canFindAllWithRelatedObjectsByNames() throws Throwable {
		assertsForUserWith_COMMUNITY_READ(() -> service.findAllWithRelatedObjects(community, "name", false, false));
	}

	@Test
	void userWith_COMMUNITY_READ_canFindAllNotExpiredByCommunityIdWithRelatedObjects() throws Throwable {
		assertsForUserWith_COMMUNITY_READ(() -> service.findAllNotExpiredByCommunityIdWithRelatedObjects(community));
	}

	@Test
	void userWithoutResourceSpecified_COMMUNITY_READ_canGetAvailableAmountForNew() throws Throwable {
		assertsForUserWith_COMMUNITY_READ_withoutResourceSpecified(() -> service.getAvailableAmountForNew(resourceCredit));
	}

	@Test
	void userWithoutResourceSpecified_COMMUNITY_READ_canGetAvailableAmountForUpdate() throws Throwable {
		assertsForUserWith_COMMUNITY_READ_withoutResourceSpecified(() -> service.getAvailableAmountForUpdate(resourceCredit, communityAllocation));
	}

	@Test
	void userWithoutResourceSpecified_COMMUNITY_WRITE_canCreate() throws Throwable {
		assertsForUserWith_COMMUNITY_WRITE_withoutResourceSpecified(() -> service.create(CommunityAllocation.builder().build()));
	}

	@Test
	void userWithoutResourceSpecified_COMMUNITY_WRITE_canUpdate() throws Throwable {
		assertsForUserWith_COMMUNITY_WRITE_withoutResourceSpecified(() -> service.update(CommunityAllocation.builder().build()));
	}

	@Test
	void userWithoutResourceSpecified_COMMUNITY_WRITE_canDelete() throws Throwable {
		assertsForUserWith_COMMUNITY_WRITE_withoutResourceSpecified(() -> service.delete(communityAllocation));
	}
}