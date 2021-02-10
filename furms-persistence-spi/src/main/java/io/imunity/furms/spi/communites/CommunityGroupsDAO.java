/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.spi.communites;

import io.imunity.furms.domain.communities.CommunityGroup;
import io.imunity.furms.domain.users.User;

import java.util.List;
import java.util.Optional;

public interface CommunityGroupsDAO {
	Optional<CommunityGroup> get(String id);

	void create(CommunityGroup community);

	void update(CommunityGroup community);

	void delete(String id);

	List<User> getAllAdmins(String id);

	void addAdmin(String communityId, String userId);

	void removeAdmin(String communityId, String userId);

	boolean isAdmin(String communityId, String userId);
}