/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.spi.communites;

import io.imunity.furms.domain.communities.Community;

import java.util.Optional;
import java.util.Set;

public interface CommunityRepository {
	Optional<Community> findById(String id);

	Set<Community> findAll();

	Community save(Community site);

	boolean exists(String id);

	boolean isUniqueUserFacingName(String name);

	void delete(String id);
}
