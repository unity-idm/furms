/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.spi.communites;

import io.imunity.furms.domain.communities.Community;

public interface UnityCommunityRepository {
	void save(Community community);
	void delete(String id);
}