/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.spi.communites;

import io.imunity.furms.domain.communities.CommunityGroup;
import io.imunity.furms.domain.users.AllUsersAndCommunityAdmins;
import io.imunity.furms.domain.users.CommunityUsersAndCommunityAdmins;
import io.imunity.furms.domain.users.FURMSUser;
import io.imunity.furms.domain.users.PersistentId;

import java.util.List;
import java.util.Optional;

public interface CommunityGroupsDAO {
	Optional<CommunityGroup> get(String id);

	void create(CommunityGroup community);

	void update(CommunityGroup community);

	void delete(String id);

	List<FURMSUser> getAllAdmins(String id);

	AllUsersAndCommunityAdmins getAllUsersAndCommunityAdmins(String communityId);

	List<FURMSUser> getAllUsers(String id);

	CommunityUsersAndCommunityAdmins getCommunityAdminsAndUsers(String communityId);

	void addAdmin(String communityId, PersistentId userId);

	void removeAdmin(String communityId, PersistentId userId);
}