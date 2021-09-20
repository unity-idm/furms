/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.api.sites;

import io.imunity.furms.domain.invitations.Invitation;
import io.imunity.furms.domain.invitations.InvitationId;
import io.imunity.furms.domain.site_agent.PendingJob;
import io.imunity.furms.domain.site_agent.SiteAgentStatus;
import io.imunity.furms.domain.sites.Site;
import io.imunity.furms.domain.users.FURMSUser;
import io.imunity.furms.domain.users.PersistentId;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface SiteService {

	boolean existsById(String id);

	Optional<Site> findById(String id);

	Set<Site> findAll();

	Set<Site> findUserSites(PersistentId userId);

	Set<Site> findAllOfCurrentUserId();

	void create(Site site);

	void update(Site site);

	void delete(String id);

	boolean isNamePresent(String name);

	boolean isNamePresentIgnoringRecord(String name, String recordToIgnore);

	List<FURMSUser> findAllSiteUsers(String id);

	List<FURMSUser> findAllAdministrators(String siteId);

	List<FURMSUser> findAllSupportUsers(String siteId);

	Set<Invitation> findSiteAdminInvitations(String siteId);

	Set<Invitation> findSiteSupportInvitations(String siteId);

	void inviteAdmin(String siteId, PersistentId userId);

	void inviteAdmin(String siteId, String email);

	void inviteSupport(String siteId, PersistentId userId);

	void inviteSupport(String siteId, String email);

	void resendInvitation(String siteId, InvitationId invitationId);

	void changeInvitationRoleToSupport(String siteId, InvitationId invitationId);

	void changeInvitationRoleToAdmin(String siteId, InvitationId invitationId);

	void removeInvitation(String siteId, InvitationId invitationId);

	void addAdmin(String siteId, PersistentId userId);

	void addSupport(String siteId, PersistentId userId);

	void removeSiteUser(String siteId, PersistentId userId);

	boolean isCurrentUserAdminOf(String siteId);

	boolean isCurrentUserSupportOf(String siteId);

	PendingJob<SiteAgentStatus> getSiteAgentStatus(String siteId);
}
