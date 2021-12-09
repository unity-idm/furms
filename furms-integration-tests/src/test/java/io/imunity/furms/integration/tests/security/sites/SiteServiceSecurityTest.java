/*
 * Copyright (c) 2021 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.integration.tests.security.sites;

import io.imunity.furms.api.sites.SiteService;
import io.imunity.furms.domain.invitations.InvitationId;
import io.imunity.furms.domain.sites.Site;
import io.imunity.furms.domain.users.PersistentId;
import io.imunity.furms.integration.tests.security.SecurityTestsBase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.UUID;

class SiteServiceSecurityTest extends SecurityTestsBase {

	@Autowired
	private SiteService service;

	@Test
	void shouldAllPublicMethodsHaveSecurityAnnotation() {
		assertThatAllInterfaceMethodsHaveBeenAnnotatedWithSecurityAnnotation(SiteService.class, service);
	}

	@Test
	void userWith_AUTHENTICATED_canCheckThatExistsById() throws Throwable {
		assertsForUserWith_AUTHENTICATED(() -> service.existsById(site));
	}

	@Test
	void userWith_SITE_READ_canFindById() throws Throwable {
		assertsForUserWith_SITE_READ(() -> service.findById(site));
	}

	@Test
	void userWithoutSpecificResource_SITE_READ_canFindAll() throws Throwable {
		assertsForUserWith_SITE_READ_withoutResourceSpecified(() -> service.findAll());
	}

	@Test
	void userWith_AUTHENTICATED_canFindUserSites() throws Throwable {
		assertsForUserWith_AUTHENTICATED(() -> service.findUserSites(new PersistentId("id")));
	}

	@Test
	void userWith_AUTHENTICATED_canFindAllOfCurrentUserId() throws Throwable {
		assertsForUserWith_AUTHENTICATED(() -> service.findAllOfCurrentUserId());
	}

	@Test
	void userWithoutResourceSpecified_SITE_WRITE_canCreate() throws Throwable {
		assertsForUserWith_SITE_WRITE_withoutResourceSpecified(() -> service.create(Site.builder().build()));
	}

	@Test
	void userWith_SITE_WRITE_canUpdate() throws Throwable {
		assertsForUserWith_SITE_WRITE(() -> service.update(Site.builder().id(site).build()));
	}

	@Test
	void userWithoutResourceSpecified_SITE_WRITE_canDelete() throws Throwable {
		assertsForUserWith_SITE_WRITE_withoutResourceSpecified(() -> service.delete(site));
	}

	@Test
	void userWithoutResourceSpecified_SITE_READ_canCheckIsNamePresent() throws Throwable {
		assertsForUserWith_SITE_READ_withoutResourceSpecified(() -> service.isNamePresent("name"));
	}

	@Test
	void userWith_SITE_READ_canCheckIsNamePresentIgnoringRecord() throws Throwable {
		assertsForUserWith_SITE_READ(() -> service.isNamePresentIgnoringRecord("name", site));
	}

	@Test
	void userWith_SITE_READ_canFindAllAdministrators() throws Throwable {
		assertsForUserWith_SITE_READ(() -> service.findAllAdministrators(site));
	}

	@Test
	void userWith_SITE_READ_canFindAllSupportUsers() throws Throwable {
		assertsForUserWith_SITE_READ(() -> service.findAllSupportUsers(site));
	}

	@Test
	void userWith_SITE_READ_canFindAllSiteUsers() throws Throwable {
		assertsForUserWith_SITE_READ(() -> service.findAllSiteUsers(site));
	}

	@Test
	void userWith_SITE_WRITE_canInviteAdmin() throws Throwable {
		assertsForUserWith_SITE_WRITE(() -> service.inviteAdmin(site, new PersistentId("id")));
	}

	@Test
	void userWith_SITE_WRITE_canInviteAdminByEmail() throws Throwable {
		assertsForUserWith_SITE_WRITE(() -> service.inviteAdmin(site, "indiana.jones@colorado.edu.us"));
	}

	@Test
	void userWith_SITE_WRITE_canInviteSupport() throws Throwable {
		assertsForUserWith_SITE_WRITE(() -> service.inviteSupport(site, new PersistentId("id")));
	}

	@Test
	void userWith_SITE_WRITE_canInviteSupportByEmail() throws Throwable {
		assertsForUserWith_SITE_WRITE(() -> service.inviteSupport(site, "robin@arkham.gotham.com"));
	}

	@Test
	void userWith_SITE_WRITE_canFindSiteAdminInvitations() throws Throwable {
		assertsForUserWith_SITE_WRITE(() -> service.findSiteAdminInvitations(site));
	}

	@Test
	void userWith_SITE_WRITE_canFindSiteSupportInvitations() throws Throwable {
		assertsForUserWith_SITE_WRITE(() -> service.findSiteSupportInvitations(site));
	}

	@Test
	void userWith_SITE_WRITE_canResendInvitation() throws Throwable {
		assertsForUserWith_SITE_WRITE(() -> service.resendInvitation(site, new InvitationId(UUID.randomUUID().toString())));
	}

	@Test
	void userWith_SITE_WRITE_canChangeInvitationRoleToSupport() throws Throwable {
		assertsForUserWith_SITE_WRITE(() -> service.changeInvitationRoleToSupport(site, new InvitationId(UUID.randomUUID().toString())));
	}

	@Test
	void userWith_SITE_WRITE_canChangeInvitationRoleToAdmin() throws Throwable {
		assertsForUserWith_SITE_WRITE(() -> service.changeInvitationRoleToAdmin(site, new InvitationId(UUID.randomUUID().toString())));
	}

	@Test
	void userWith_SITE_WRITE_canRemoveInvitation() throws Throwable {
		assertsForUserWith_SITE_WRITE(() -> service.removeInvitation(site, new InvitationId(UUID.randomUUID().toString())));
	}

	@Test
	void userWith_SITE_WRITE_canAddAdmin() throws Throwable {
		assertsForUserWith_SITE_WRITE(() -> service.addAdmin(site, new PersistentId("id")));
	}

	@Test
	void userWith_SITE_WRITE_canAddSupport() throws Throwable {
		assertsForUserWith_SITE_WRITE(() -> service.addSupport(site, new PersistentId("id")));
	}

	@Test
	void userWith_SITE_WRITE_canChangeRoleToAdmin() throws Throwable {
		assertsForUserWith_SITE_WRITE(() -> service.changeRoleToAdmin(site, new PersistentId("id")));
	}

	@Test
	void userWith_SITE_WRITE_canChangeRoleToSupport() throws Throwable {
		assertsForUserWith_SITE_WRITE(() -> service.changeRoleToSupport(site, new PersistentId("id")));
	}

	@Test
	void userWith_SITE_WRITE_canRemoveSiteUser() throws Throwable {
		assertsForUserWith_SITE_WRITE(() -> service.removeSiteUser(site, new PersistentId("id")));
	}

	@Test
	void userWith_SITE_READ_canCheckIsCurrentUserAdminOf() throws Throwable {
		assertsForUserWith_SITE_READ(() -> service.isCurrentUserAdminOf(site));
	}

	@Test
	void userWith_SITE_READ_canCheckIsCurrentUserSupportOf() throws Throwable {
		assertsForUserWith_SITE_READ(() -> service.isCurrentUserSupportOf(site));
	}
}
