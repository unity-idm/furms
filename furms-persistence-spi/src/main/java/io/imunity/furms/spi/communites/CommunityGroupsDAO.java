/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.spi.communites;

import io.imunity.furms.domain.communities.CommunityGroup;

import java.util.Optional;

public interface CommunityGroupsDAO {
	Optional<CommunityGroup> get(String id);

	void create(CommunityGroup community);

	void update(CommunityGroup community);

	void delete(String id);
}