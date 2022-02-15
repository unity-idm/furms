/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.db.projects;

import org.springframework.data.repository.CrudRepository;

import java.util.Set;
import java.util.UUID;

interface ProjectEntityRepository extends CrudRepository<ProjectEntity, UUID> {

	Set<ProjectEntity> findAllByIdIn(Set<UUID> ids);
	Set<ProjectEntity> findAllByCommunityId(UUID communityId);
	Set<ProjectEntity> findAllByCommunityIdIn(Set<UUID> communityIds);
	boolean existsByCommunityIdAndName(UUID communityId, String name);
	boolean existsByCommunityIdAndId(UUID communityId, UUID id);
}
