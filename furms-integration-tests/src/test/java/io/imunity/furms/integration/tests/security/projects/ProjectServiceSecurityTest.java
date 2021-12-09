/*
 * Copyright (c) 2021 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.integration.tests.security.projects;

import io.imunity.furms.api.projects.ProjectService;
import io.imunity.furms.domain.images.FurmsImage;
import io.imunity.furms.domain.invitations.InvitationId;
import io.imunity.furms.domain.projects.Project;
import io.imunity.furms.domain.projects.ProjectAdminControlledAttributes;
import io.imunity.furms.domain.users.PersistentId;
import io.imunity.furms.integration.tests.security.SecurityTestsBase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.UUID;

class ProjectServiceSecurityTest extends SecurityTestsBase {

	@Autowired
	private ProjectService service;

	@Test
	void shouldAllPublicMethodsHaveSecurityAnnotation() {
		assertThatAllInterfaceMethodsHaveBeenAnnotatedWithSecurityAnnotation(ProjectService.class, service);
	}

	@Test
	void userWith_AUTHENTICATED_canCheckThatExistsById() throws Throwable {
		assertsForUserWith_AUTHENTICATED(() -> service.existsById(project));
	}

	@Test
	void userWith_PROJECT_READ_canFindById() throws Throwable {
		assertsForUserWith_PROJECT_READ(() -> service.findById(project));
	}

	@Test
	void userWith_COMMUNITY_READ_canFindAllByCommunityId() throws Throwable {
		assertsForUserWith_COMMUNITY_READ(() -> service.findAllByCommunityId(community));
	}

	@Test
	void userWith_COMMUNITY_READ_canFindAllNotExpiredByCommunityId() throws Throwable {
		assertsForUserWith_COMMUNITY_READ(() -> service.findAllNotExpiredByCommunityId(community));
	}

	@Test
	void userWith_PROJECT_LIMITED_READ_canFindAll() throws Throwable {
		assertsForUserWith_PROJECT_LIMITED_READ(() -> service.findAll());
	}

	@Test
	void userWith_AUTHENTICATED_canFindAllByCurrentUserId() throws Throwable {
		assertsForUserWith_AUTHENTICATED(() -> service.findAllByCurrentUserId());
	}

	@Test
	void userWith_PROJECT_READ_canCheckIsProjectInTerminalState() throws Throwable {
		assertsForUserWith_PROJECT_READ(() -> service.isProjectInTerminalState(project));
	}

	@Test
	void userWith_COMMUNITY_READ_canCheckIsProjectInTerminalState() throws Throwable {
		assertsForUserWith_COMMUNITY_READ(() -> service.isProjectInTerminalState(community, project));
	}

	@Test
	void userWith_PROJECT_READ_canCheckIsProjectExpired() throws Throwable {
		assertsForUserWith_PROJECT_READ(() -> service.isProjectExpired(project));
	}

	@Test
	void userWith_COMMUNITY_WRITE_canCreate() throws Throwable {
		assertsForUserWith_COMMUNITY_WRITE(() -> service.create(Project.builder().communityId(community).build()));
	}

	@Test
	void userWith_PROJECT_WRITE_canUpdate() throws Throwable {
		assertsForUserWith_PROJECT_WRITE(() -> service.update(Project.builder().id(project).build()));
	}

	@Test
	void userWith_PROJECT_LIMITED_WRITE_canUpdateWithAttributes() throws Throwable {
		assertsForUserWith_PROJECT_LIMITED_WRITE(() -> service.update(
				new ProjectAdminControlledAttributes(project, "description", "researchField", FurmsImage.empty())
		));
	}

	@Test
	void userWith_COMMUNITY_WRITE_canDelete() throws Throwable {
		assertsForUserWith_COMMUNITY_WRITE(() -> service.delete(project, community));
	}

	@Test
	void userWith_PROJECT_READ_canFindAllAdmins() throws Throwable {
		assertsForUserWith_PROJECT_READ(() -> service.findAllAdmins(community, project));
	}

	@Test
	void userWith_PROJECT_READ_canCheckIsAdmin() throws Throwable {
		assertsForUserWith_PROJECT_READ(() -> service.isAdmin(project));
	}

	@Test
	void userWith_PROJECT_READ_canCheckThatHasAdminRights() throws Throwable {
		assertsForUserWith_PROJECT_READ(() -> service.hasAdminRights(project));
	}

	@Test
	void userWith_PROJECT_ADMINS_MANAGEMENT_canAddAdmin() throws Throwable {
		assertsForUserWith_PROJECT_ADMINS_MANAGEMENT(() -> service.addAdmin(community, project, new PersistentId("id")));
	}

	@Test
	void userWith_PROJECT_ADMINS_MANAGEMENT_canFindAllAdminsInvitations() throws Throwable {
		assertsForUserWith_PROJECT_ADMINS_MANAGEMENT(() -> service.findAllAdminsInvitations(project));
	}

	@Test
	void userWith_PROJECT_ADMINS_MANAGEMENT_canFindAllUsersInvitations() throws Throwable {
		assertsForUserWith_PROJECT_ADMINS_MANAGEMENT(() -> service.findAllUsersInvitations(project));
	}

	@Test
	void userWith_PROJECT_ADMINS_MANAGEMENT_canInviteAdminByPersistentId() throws Throwable {
		assertsForUserWith_PROJECT_ADMINS_MANAGEMENT(() -> service.inviteAdmin(project, new PersistentId("id")));
	}

	@Test
	void userWith_PROJECT_ADMINS_MANAGEMENT_canInviteAdminByEmail() throws Throwable {
		assertsForUserWith_PROJECT_ADMINS_MANAGEMENT(() -> service.inviteAdmin(project, "darth.vader@siths.com"));
	}

	@Test
	void userWith_PROJECT_ADMINS_MANAGEMENT_canRemoveAdmin() throws Throwable {
		assertsForUserWith_PROJECT_ADMINS_MANAGEMENT(() -> service.removeAdmin(community, project, new PersistentId("id")));
	}

	@Test
	void userWith_PROJECT_READ_canFindAllUsers() throws Throwable {
		assertsForUserWith_PROJECT_READ(() -> service.findAllUsers(community, project));
	}

	@Test
	void userWith_AUTHENTICATED_canFindProjectLeaderInfoAsInstalledUser() throws Throwable {
		assertsForUserWith_AUTHENTICATED(() -> service.findProjectLeaderInfoAsInstalledUser(project));
	}

	@Test
	void userWith_AUTHENTICATED_canCheckIsUser() throws Throwable {
		assertsForUserWith_AUTHENTICATED(() -> service.isUser(project));
	}

	@Test
	void userWith_PROJECT_LIMITED_WRITE_canAddUser() throws Throwable {
		assertsForUserWith_PROJECT_LIMITED_WRITE(() -> service.addUser(community, project, new PersistentId("id")));
	}

	@Test
	void userWith_PROJECT_LIMITED_WRITE_canInviteUser() throws Throwable {
		assertsForUserWith_PROJECT_LIMITED_WRITE(() -> service.inviteUser(project, new PersistentId("id")));
	}

	@Test
	void userWith_PROJECT_LIMITED_WRITE_canInviteUserByEmail() throws Throwable {
		assertsForUserWith_PROJECT_LIMITED_WRITE(() -> service.inviteUser(project, "obi.wam.kenobi@jedi.gov"));
	}

	@Test
	void userWith_PROJECT_LIMITED_WRITE_canResendInvitation() throws Throwable {
		assertsForUserWith_PROJECT_LIMITED_WRITE(() -> service.resendInvitation(project, new InvitationId(UUID.randomUUID().toString())));
	}

	@Test
	void userWith_PROJECT_LIMITED_WRITE_canRemoveInvitation() throws Throwable {
		assertsForUserWith_PROJECT_LIMITED_WRITE(() -> service.removeInvitation(project, new InvitationId(UUID.randomUUID().toString())));
	}

	@Test
	void userWith_PROJECT_LIMITED_WRITE_canRemoveUser() throws Throwable {
		assertsForUserWith_PROJECT_LIMITED_WRITE(() -> service.removeUser(community, project, new PersistentId("id")));
	}

	@Test
	void userWith_PROJECT_LEAVE_canResignFromMembership() throws Throwable {
		assertsForUserWith_PROJECT_LEAVE(() -> service.resignFromMembership(community, project));
	}

}