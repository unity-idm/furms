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

import static io.imunity.furms.integration.tests.security.SecurityTestRulesValidator.forMethods;
import static io.imunity.furms.integration.tests.tools.users.TestUsersProvider.basicUser;
import static io.imunity.furms.integration.tests.tools.users.TestUsersProvider.communityAdmin;
import static io.imunity.furms.integration.tests.tools.users.TestUsersProvider.fenixAdmin;
import static io.imunity.furms.integration.tests.tools.users.TestUsersProvider.projectAdmin;
import static io.imunity.furms.integration.tests.tools.users.TestUsersProvider.projectUser;
import static io.imunity.furms.integration.tests.tools.users.TestUsersProvider.siteAdmin;
import static io.imunity.furms.integration.tests.tools.users.TestUsersProvider.siteSupport;

class CommunityServiceSecurityTest extends SecurityTestsBase {

	@Autowired
	private CommunityService service;

	@Test
	void shouldAllPublicMethodsHaveSecurityAnnotation() {
		assertThatAllInterfaceMethodsHaveBeenAnnotatedWithSecurityAnnotation(CommunityService.class, service);
	}

	@Test
	void shouldPassForSecurityRulesForMethodsInCommunityService() {
		forMethods(
				() -> service.existsById(community),
				() -> service.findAllOfCurrentUser())
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
				.validate(server);
		forMethods(
				() -> service.findById(community),
				() -> service.findAllUsers(community),
				() -> service.findAllAdmins(community),
				() -> service.isAdmin(community),
				() -> service.update(Community.builder().id(community).build()),
				() -> service.findAllInvitations(community),
				() -> service.inviteAdmin(community, persistentId),
				() -> service.inviteAdmin(community, "emperor.palpatine@empire.gov"),
				() -> service.resendInvitation(community, new InvitationId(UUID.randomUUID().toString())),
				() -> service.removeInvitation(community, new InvitationId(UUID.randomUUID().toString())),
				() -> service.addAdmin(community, persistentId),
				() -> service.removeAdmin(community, persistentId))
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
				.validate(server);
		forMethods(
				() -> service.findAll(),
				() -> service.create(Community.builder().id(community).build()),
				() -> service.delete(community))
				.accessFor(
						fenixAdmin())
				.deniedFor(
						basicUser(),
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
				.validate(server);
	}

}