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

class ProjectServiceSecurityTest extends SecurityTestsBase {

	@Autowired
	private ProjectService service;

	@Test
	void shouldAllPublicMethodsHaveSecurityAnnotation() {
		assertThatAllInterfaceMethodsHaveBeenAnnotatedWithSecurityAnnotation(ProjectService.class, service);
	}

	@Test
	void shouldPassForSecurityRulesForMethodsInProjectService() {
		forMethods(
				() -> service.existsById(project),
				() -> service.findAll(),
				() -> service.findAllByCurrentUserId(),
				() -> service.findProjectLeaderInfoAsInstalledUser(project),
				() -> service.isUser(project))
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
		.andForMethods(
				() -> service.update(new ProjectAdminControlledAttributes(
						project, "description", "researchField", FurmsImage.empty())),
				() -> service.addUser(community, project, persistentId),
				() -> service.inviteUser(project, persistentId),
				() -> service.inviteUser(project, "obi.wam.kenobi@jedi.gov"),
				() -> service.resendInvitation(project, new InvitationId(UUID.randomUUID().toString())),
				() -> service.removeInvitation(project, new InvitationId(UUID.randomUUID().toString())),
				() -> service.removeUser(community, project, persistentId),
				() -> service.addAdmin(community, project, persistentId),
				() -> service.findAllAdminsInvitations(project),
				() -> service.findAllUsersInvitations(project),
				() -> service.inviteAdmin(project, persistentId),
				() -> service.inviteAdmin(project, "darth.vader@siths.com"),
				() -> service.removeAdmin(community, project, persistentId))
				.accessFor(
						communityAdmin(community),
						projectAdmin(community, project))
				.deniedFor(
						basicUser(),
						fenixAdmin(),
						siteAdmin(site),
						siteAdmin(otherSite),
						siteSupport(site),
						siteSupport(otherSite),
						communityAdmin(otherCommunity),
						projectAdmin(otherCommunity, otherProject),
						projectUser(community, project),
						projectUser(otherCommunity, otherProject))
		.andForMethods(
				() -> service.findById(project),
				() -> service.isProjectInTerminalState(project),
				() -> service.isProjectExpired(project),
				() -> service.findAllAdmins(community, project),
				() -> service.isAdmin(project),
				() -> service.hasAdminRights(project),
				() -> service.findAllUsers(community, project),
				() -> service.findAllUsers(project),
				() -> service.resignFromMembership(community, project))
				.accessFor(
						communityAdmin(community),
						projectAdmin(community, project),
						projectUser(community, project))
				.deniedFor(
						basicUser(),
						fenixAdmin(),
						siteAdmin(site),
						siteAdmin(otherSite),
						siteSupport(site),
						siteSupport(otherSite),
						communityAdmin(otherCommunity),
						projectAdmin(otherCommunity, otherProject),
						projectUser(otherCommunity, otherProject))
		.andForMethods(
				() -> service.update(Project.builder().id(project).build()))
				.accessFor(
						communityAdmin(community))
				.deniedFor(
						basicUser(),
						fenixAdmin(),
						siteAdmin(site),
						siteAdmin(otherSite),
						siteSupport(site),
						siteSupport(otherSite),
						communityAdmin(otherCommunity),
						projectAdmin(community, project),
						projectAdmin(otherCommunity, otherProject),
						projectUser(community, project),
						projectUser(otherCommunity, otherProject))
		.andForMethods(
				() -> service.findAllByCommunityId(community),
				() -> service.findAllNotExpiredByCommunityId(community),
				() -> service.isProjectInTerminalState(community, project),
				() -> service.create(Project.builder().communityId(community).build()),
				() -> service.delete(project, community))
				.accessFor(
						fenixAdmin(),
						communityAdmin(community))
				.deniedFor(
						basicUser(),
						siteAdmin(site),
						siteAdmin(otherSite),
						siteSupport(site),
						siteSupport(otherSite),
						communityAdmin(otherCommunity),
						projectAdmin(community, project),
						projectAdmin(otherCommunity, otherProject),
						projectUser(community, project),
						projectUser(otherCommunity, otherProject))
		.verifySecurityRulesAndInterfaceCoverage(ProjectService.class, server);
	}

}