/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.api.communites;

import io.imunity.furms.domain.communities.Community;
import io.imunity.furms.domain.users.FURMSUser;
import io.imunity.furms.domain.users.PersistentId;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface CommunityService {
	Optional<Community> findById(String id);

	Set<Community> findAll();

	void create(Community community);

	void update(Community community);

	void delete(String id);

	List<FURMSUser> findAllAdmins(String communityId);

	void inviteAdmin(String communityId, PersistentId userId);

	void addAdmin(String communityId, PersistentId userId);

	void removeAdmin(String communityId, PersistentId userId);

	boolean isAdmin(String communityId);
}
