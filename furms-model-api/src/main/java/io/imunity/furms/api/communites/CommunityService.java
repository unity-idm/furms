/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.api.communites;

import io.imunity.furms.domain.communities.Community;
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
	boolean existsById(String id);

	Set<Community> findAll(Set<String> ids);

	Optional<Community> findById(String id);

	Set<Community> findAll();

	Set<Community> findAllOfCurrentUser();

	void create(Community community);

	void update(Community community);

	void delete(String id);

	List<FURMSUser> findAllAdmins(String communityId);

	CommunityUsersAndAdmins findAllCommunityAdminsAllUsers(String id);

	List<FURMSUser> findAllUsers(String communityId);

	AllUsersAndCommunityAdmins findAllAdminsWithAllUsers(String id);

	Set<Invitation> findAllInvitations(String communityId);

	void inviteAdmin(String communityId, PersistentId userId);

	void inviteAdmin(String communityId, String email);

	void resendInvitation(String communityId, InvitationId invitationId);

	void removeInvitation(String communityId, InvitationId invitationId);

	void addAdmin(String communityId, PersistentId userId);

	void removeAdmin(String communityId, PersistentId userId);

	boolean isAdmin(String communityId);
}
