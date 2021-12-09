/*
 * Copyright (c) 2021 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.integration.tests.security.communities;

import io.imunity.furms.api.communites.CommunityService;
import io.imunity.furms.domain.communities.Community;
import io.imunity.furms.domain.invitations.InvitationId;
import io.imunity.furms.domain.users.PersistentId;
import io.imunity.furms.integration.tests.security.SecurityTestsBase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.UUID;

class CommunityServiceSecurityTest extends SecurityTestsBase {

	@Autowired
	private CommunityService service;

	@Test
	void shouldAllPublicMethodsHaveSecurityAnnotation() {
		assertThatAllInterfaceMethodsHaveBeenAnnotatedWithSecurityAnnotation(CommunityService.class, service);
	}

	@Test
	void userWith_AUTHENTICATED_canCheckThatExistsById() throws Throwable {
		assertsForUserWith_AUTHENTICATED(() -> service.existsById(community));
	}

	@Test
	void userWith_COMMUNITY_READ_canFindById() throws Throwable {
		assertsForUserWith_COMMUNITY_READ(() -> service.findById(community));
	}

	@Test
	void userWithoutResourceSpecified_COMMUNITY_READ_canFindAll() throws Throwable {
		assertsForUserWith_COMMUNITY_READ_withoutResourceSpecified(() -> service.findAll());
	}

	@Test
	void userWith_AUTHENTICATED_canFindAllOfCurrentUser() throws Throwable {
		assertsForUserWith_AUTHENTICATED(() -> service.findAllOfCurrentUser());
	}

	@Test
	void userWithoutResourceSpecified_COMMUNITY_WRITE_canCreate() throws Throwable {
		assertsForUserWith_COMMUNITY_WRITE_withoutResourceSpecified(
				() -> service.create(Community.builder().id(community).build()));
	}

	@Test
	void userWith_COMMUNITY_WRITE_canUpdate() throws Throwable {
		assertsForUserWith_COMMUNITY_WRITE(() -> service.update(Community.builder().id(community).build()));
	}

	@Test
	void userWithoutResourceSpecified_COMMUNITY_WRITE_canDelete() throws Throwable {
		assertsForUserWith_COMMUNITY_WRITE_withoutResourceSpecified(() -> service.delete(community));
	}

	@Test
	void userWith_COMMUNITY_READ_canFindAllAdmins() throws Throwable {
		assertsForUserWith_COMMUNITY_READ(() -> service.findAllAdmins(community));
	}

	@Test
	void userWith_COMMUNITY_WRITE_canFindAllInvitations() throws Throwable {
		assertsForUserWith_COMMUNITY_WRITE(() -> service.findAllInvitations(community));
	}

	@Test
	void userWith_COMMUNITY_WRITE_canInviteAdminByPersistentId() throws Throwable {
		assertsForUserWith_COMMUNITY_WRITE(() -> service.inviteAdmin(community, new PersistentId("id")));
	}

	@Test
	void userWith_COMMUNITY_WRITE_canInviteAdminByEmail() throws Throwable {
		assertsForUserWith_COMMUNITY_WRITE(() -> service.inviteAdmin(community, "emperor.palpatine@empire.gov"));
	}

	@Test
	void userWith_COMMUNITY_WRITE_canResendInvitation() throws Throwable {
		assertsForUserWith_COMMUNITY_WRITE(() -> service.resendInvitation(community, new InvitationId(UUID.randomUUID().toString())));
	}

	@Test
	void userWith_COMMUNITY_WRITE_canRemoveInvitation() throws Throwable {
		assertsForUserWith_COMMUNITY_WRITE(() -> service.removeInvitation(community, new InvitationId(UUID.randomUUID().toString())));
	}

	@Test
	void userWith_COMMUNITY_WRITE_canAddAdmin() throws Throwable {
		assertsForUserWith_COMMUNITY_WRITE(() -> service.addAdmin(community, new PersistentId("test")));
	}

	@Test
	void userWith_COMMUNITY_WRITE_canRemoveAdmin() throws Throwable {
		assertsForUserWith_COMMUNITY_WRITE(() -> service.removeAdmin(community, new PersistentId("test")));
	}

	@Test
	void userWith_COMMUNITY_READ_canCheckIsAdmin() throws Throwable {
		assertsForUserWith_COMMUNITY_READ(() -> service.isAdmin(community));
	}

}