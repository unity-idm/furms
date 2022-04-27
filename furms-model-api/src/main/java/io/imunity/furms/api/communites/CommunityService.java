/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.api.communites;

import io.imunity.furms.domain.communities.Community;
import io.imunity.furms.domain.communities.CommunityId;
import io.imunity.furms.domain.invitations.Invitation;
import io.imunity.furms.domain.invitations.InvitationId;
import io.imunity.furms.domain.users.AllUsersAndCommunityAdmins;
import io.imunity.furms.domain.users.CommunityUsersAndAdmins;
import io.imunity.furms.domain.users.FURMSUser;
import io.imunity.furms.domain.users.PersistentId;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface CommunityService {
	boolean existsById(CommunityId id);

	Set<Community> findAll(Set<CommunityId> ids);

	Optional<Community> findById(CommunityId id);

	Set<Community> findAll();

	Set<Community> findAllOfCurrentUser();

	void create(Community community);

	void update(Community community);

	void delete(CommunityId id);

	List<FURMSUser> findAllAdmins(CommunityId communityId);

	CommunityUsersAndAdmins findAllCommunityAdminsAllUsers(CommunityId id);

	List<FURMSUser> findAllUsers(CommunityId communityId);

	AllUsersAndCommunityAdmins findAllAdminsWithAllUsers(CommunityId id);

	Set<Invitation> findAllInvitations(CommunityId communityId);

	void inviteAdmin(CommunityId communityId, PersistentId userId);

	void inviteAdmin(CommunityId communityId, String email);

	void resendInvitation(CommunityId communityId, InvitationId invitationId);

	void removeInvitation(CommunityId communityId, InvitationId invitationId);

	void addAdmin(CommunityId communityId, PersistentId userId);

	void removeAdmin(CommunityId communityId, PersistentId userId);

	boolean isAdmin(CommunityId communityId);
}
