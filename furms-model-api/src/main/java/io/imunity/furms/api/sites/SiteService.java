/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.api.sites;

import io.imunity.furms.domain.invitations.Invitation;
import io.imunity.furms.domain.invitations.InvitationId;
import io.imunity.furms.domain.sites.Site;
import io.imunity.furms.domain.sites.SiteId;
import io.imunity.furms.domain.users.AllUsersAndSiteAdmins;
import io.imunity.furms.domain.users.FURMSUser;
import io.imunity.furms.domain.users.PersistentId;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface SiteService {

	boolean existsById(SiteId id);

	Optional<Site> findById(SiteId id);

	Set<Site> findAll(Set<SiteId> ids);

	Set<Site> findAll();

	Set<Site> findUserSites(PersistentId userId);

	Set<Site> findAllOfCurrentUserId();

	void create(Site site);

	void updateName(SiteId siteId, String name);

	void update(Site site);

	void delete(SiteId id);

	boolean isNamePresent(String name);

	boolean isNamePresentIgnoringRecord(String name, SiteId siteId);

	List<FURMSUser> findAllSiteUsers(SiteId id);

	AllUsersAndSiteAdmins findAllUsersAndSiteAdmins(SiteId id);

	Set<Invitation> findSiteAdminInvitations(SiteId siteId);

	Set<Invitation> findSiteSupportInvitations(SiteId siteId);

	void inviteAdmin(SiteId siteId, PersistentId userId);

	void inviteAdmin(SiteId siteId, String email);

	void inviteSupport(SiteId siteId, PersistentId userId);

	void inviteSupport(SiteId siteId, String email);

	void resendInvitation(SiteId siteId, InvitationId invitationId);

	void changeInvitationRoleToSupport(SiteId siteId, InvitationId invitationId);

	void changeInvitationRoleToAdmin(SiteId siteId, InvitationId invitationId);

	void removeInvitation(SiteId siteId, InvitationId invitationId);

	void addAdmin(SiteId siteId, PersistentId userId);

	void addSupport(SiteId siteId, PersistentId userId);

	void changeRoleToAdmin(SiteId siteId, PersistentId userId);

	void changeRoleToSupport(SiteId siteId, PersistentId userId);

	void removeSiteUser(SiteId siteId, PersistentId userId);

	boolean isCurrentUserAdminOf(SiteId siteId);

	boolean isCurrentUserSupportOf(SiteId siteId);
}
