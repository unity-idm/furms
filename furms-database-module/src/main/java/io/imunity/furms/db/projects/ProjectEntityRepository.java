/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.db.projects;

import org.springframework.data.repository.CrudRepository;

import java.util.UUID;
import java.util.stream.Stream;

interface ProjectEntityRepository extends CrudRepository<ProjectEntity, UUID> {

	Stream<ProjectEntity> findAllByCommunityId(UUID communityId);
	boolean existsByName(String name);
}
