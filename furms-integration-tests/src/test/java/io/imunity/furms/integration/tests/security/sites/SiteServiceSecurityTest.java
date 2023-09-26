/*
 * Copyright (c) 2021 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.integration.tests.security.sites;

import io.imunity.furms.api.sites.SiteService;
import io.imunity.furms.domain.invitations.InvitationId;
import io.imunity.furms.domain.sites.Site;
import io.imunity.furms.integration.tests.security.SecurityTestsBase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Set;
import java.util.UUID;

import static io.imunity.furms.integration.tests.security.SecurityTestRulesValidator.forMethods;
import static io.imunity.furms.integration.tests.tools.users.TestUsersProvider.basicUser;
import static io.imunity.furms.integration.tests.tools.users.TestUsersProvider.communityAdmin;
import static io.imunity.furms.integration.tests.tools.users.TestUsersProvider.fenixAdmin;
import static io.imunity.furms.integration.tests.tools.users.TestUsersProvider.projectAdmin;
import static io.imunity.furms.integration.tests.tools.users.TestUsersProvider.projectUser;
import static io.imunity.furms.integration.tests.tools.users.TestUsersProvider.siteAdmin;
import static io.imunity.furms.integration.tests.tools.users.TestUsersProvider.siteSupport;

class SiteServiceSecurityTest extends SecurityTestsBase {

	@Autowired
	private SiteService service;

	@Test
	void shouldAllPublicMethodsHaveSecurityAnnotation() {
		assertThatAllInterfaceMethodsHaveBeenAnnotatedWithSecurityAnnotation(SiteService.class, service);
	}

	@Test
	void shouldPassForSecurityRulesForMethodsInSiteService() {
		final InvitationId invitationId = new InvitationId(UUID.randomUUID().toString());
		forMethods(
				() -> service.existsById(site),
				() -> service.findUserSites(persistentId),
				() -> service.findAllOfCurrentUserId())
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
				() -> service.findById(site),
				() -> service.isNamePresentIgnoringRecord("name", site),
				() -> service.findAllSiteUsers(site),
				() -> service.findAll(Set.of(site)),
				() -> service.isCurrentUserAdminOf(site),
				() -> service.isCurrentUserSupportOf(site))
				.accessFor(
						fenixAdmin(),
						siteAdmin(site),
						siteSupport(site))
				.deniedFor(
						basicUser(),
						siteAdmin(otherSite),
						siteSupport(otherSite),
						communityAdmin(community),
						communityAdmin(otherCommunity),
						projectAdmin(community, project),
						projectAdmin(otherCommunity, otherProject),
						projectUser(community, project),
						projectUser(otherCommunity, otherProject))
		.andForMethods(
				() -> service.isNamePresent("name"),
				() -> service.findAll(),
				() -> service.findAllUsersAndSiteAdmins(site),
				() -> service.create(Site.builder().build()),
				() -> service.delete(site))
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
		.andForMethods(
				() -> service.updateName(site, "name"),
				() -> service.update(Site.builder().id(site).build()),
				() -> service.inviteAdmin(site, persistentId),
				() -> service.inviteAdmin(site, "indiana.jones@colorado.edu.us"),
				() -> service.inviteSupport(site, persistentId),
				() -> service.inviteSupport(site, "robin@arkham.gotham.com"),
				() -> service.findSiteAdminInvitations(site),
				() -> service.findSiteSupportInvitations(site),
				() -> service.resendInvitation(site, invitationId),
				() -> service.changeInvitationRoleToSupport(site, invitationId),
				() -> service.changeInvitationRoleToAdmin(site, invitationId),
				() -> service.removeInvitation(site, invitationId),
				() -> service.addAdmin(site, persistentId),
				() -> service.addSupport(site, persistentId),
				() -> service.changeRoleToAdmin(site, persistentId),
				() -> service.changeRoleToSupport(site, persistentId),
				() -> service.removeSiteUser(site, persistentId))
				.accessFor(
						fenixAdmin(),
						siteAdmin(site))
				.deniedFor(
						basicUser(),
						siteAdmin(otherSite),
						siteSupport(site),
						siteSupport(otherSite),
						communityAdmin(community),
						communityAdmin(otherCommunity),
						projectAdmin(community, project),
						projectAdmin(otherCommunity, otherProject),
						projectUser(community, project),
						projectUser(otherCommunity, otherProject))
		.verifySecurityRulesAndInterfaceCoverage(SiteService.class, server);
	}
}
