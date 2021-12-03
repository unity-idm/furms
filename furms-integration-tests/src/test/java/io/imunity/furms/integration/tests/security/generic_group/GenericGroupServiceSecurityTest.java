/*
 * Copyright (c) 2021 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.integration.tests.security.generic_group;

import io.imunity.furms.api.generic_groups.GenericGroupService;
import io.imunity.furms.domain.generic_groups.GenericGroup;
import io.imunity.furms.domain.generic_groups.GenericGroupId;
import io.imunity.furms.domain.users.FenixUserId;
import io.imunity.furms.integration.tests.security.SecurityTestsBase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.UUID;

class GenericGroupServiceSecurityTest extends SecurityTestsBase {

	@Autowired
	private GenericGroupService service;

	@Test
	void shouldAllPublicMethodsHaveSecurityAnnotation() {
		assertThatAllPublicMethodsBeAnnotatedWithSecurityAnnotation(service.getClass());
	}

	@Test
	void userWith_COMMUNITY_READ_canFindAll() throws Throwable {
		assertsForUserWith_COMMUNITY_READ(() -> service.findAll(community));
	}

	@Test
	void userWith_COMMUNITY_READ_canFindGroupWithAssignments() throws Throwable {
		assertsForUserWith_COMMUNITY_READ(() -> service.findGroupWithAssignments(community,
				new GenericGroupId(UUID.randomUUID().toString())));
	}

	@Test
	void userWith_COMMUNITY_READ_canFindBy() throws Throwable {
		assertsForUserWith_COMMUNITY_READ(() -> service.findBy(community, new GenericGroupId(UUID.randomUUID().toString())));
	}

	@Test
	void userWith_COMMUNITY_READ_canFindAllGroupWithAssignmentsAmount() throws Throwable {
		assertsForUserWith_COMMUNITY_READ(() -> service.findAllGroupWithAssignmentsAmount(community));
	}

	@Test
	void userWith_MEMBERSHIP_GROUP_READ_canFindAll() throws Throwable {
		assertsForUserWith_MEMBERSHIP_GROUP_READ(() -> service.findAll(community, new GenericGroupId(UUID.randomUUID().toString())));
	}

	@Test
	void userWith_COMMUNITY_WRITE_canCreate() throws Throwable {
		assertsForUserWith_COMMUNITY_WRITE(() -> service.create(GenericGroup.builder().communityId(community).build()));
	}

	@Test
	void userWith_MEMBERSHIP_GROUP_WRITE_canCreateMembership() throws Throwable {
		assertsForUserWith_MEMBERSHIP_GROUP_WRITE(() -> service.createMembership(community,
				new GenericGroupId(UUID.randomUUID().toString()), new FenixUserId("Fawkes")));
	}

	@Test
	void userWith_COMMUNITY_WRITE_canUpdate() throws Throwable {
		assertsForUserWith_COMMUNITY_WRITE(() -> service.update(GenericGroup.builder().communityId(community).build()));
	}

	@Test
	void userWith_COMMUNITY_WRITE_canDelete() throws Throwable {
		assertsForUserWith_COMMUNITY_WRITE(() -> service.delete(community, new GenericGroupId(UUID.randomUUID().toString())));
	}

	@Test
	void userWith_MEMBERSHIP_GROUP_WRITE_canDeleteMembership() throws Throwable {
		assertsForUserWith_MEMBERSHIP_GROUP_WRITE(() -> service.deleteMembership(community,
				new GenericGroupId(UUID.randomUUID().toString()), new FenixUserId("Fawkes")));
	}
}