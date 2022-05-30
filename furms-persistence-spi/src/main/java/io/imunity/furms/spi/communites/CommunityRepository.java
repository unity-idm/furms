/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.spi.communites;

import io.imunity.furms.domain.communities.Community;
import io.imunity.furms.domain.communities.CommunityId;

import java.util.Optional;
import java.util.Set;

public interface CommunityRepository {
	Optional<Community> findById(CommunityId id);

	Set<Community> findAll(Set<CommunityId> ids);

	Set<Community> findAll();

	CommunityId create(Community community);

	void update(Community community);

	boolean exists(CommunityId id);

	boolean isUniqueName(String name);

	void delete(CommunityId id);
	
	void deleteAll();
}
