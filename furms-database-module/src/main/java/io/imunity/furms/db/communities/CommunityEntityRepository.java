/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.db.communities;

import org.springframework.data.repository.CrudRepository;

import java.util.UUID;

interface CommunityEntityRepository extends CrudRepository<CommunityEntity, UUID> {

	boolean existsByName(String name);
}
