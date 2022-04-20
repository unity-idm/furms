/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.spi.communites;

import io.imunity.furms.domain.communities.CommunityGroup;
import io.imunity.furms.domain.communities.CommunityId;
import io.imunity.furms.domain.users.AllUsersAndCommunityAdmins;
import io.imunity.furms.domain.users.CommunityUsersAndAdmins;
import io.imunity.furms.domain.users.FURMSUser;
import io.imunity.furms.domain.users.PersistentId;

import java.util.List;
import java.util.Optional;

public interface CommunityGroupsDAO {
	Optional<CommunityGroup> get(CommunityId id);

	void create(CommunityGroup community);

	void update(CommunityGroup community);

	void delete(CommunityId id);

	List<FURMSUser> getAllAdmins(CommunityId id);

	AllUsersAndCommunityAdmins getAllUsersAndCommunityAdmins(CommunityId communityId);

	List<FURMSUser> getAllUsers(CommunityId id);

	CommunityUsersAndAdmins getCommunityAdminsAndUsers(CommunityId communityId);

	void addAdmin(CommunityId communityId, PersistentId userId);

	void removeAdmin(CommunityId communityId, PersistentId userId);
}