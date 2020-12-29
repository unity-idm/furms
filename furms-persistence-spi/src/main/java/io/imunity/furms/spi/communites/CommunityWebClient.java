/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.spi.communites;

import io.imunity.furms.domain.communities.Community;

import java.util.Optional;

public interface CommunityWebClient {
	Optional<Community> get(String id);

	void create(Community community);

	void update(Community community);

	void delete(String id);
}