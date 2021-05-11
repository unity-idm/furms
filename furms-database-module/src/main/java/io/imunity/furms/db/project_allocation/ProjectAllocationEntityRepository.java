/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.db.project_allocation;

import org.springframework.data.repository.CrudRepository;

import java.util.Set;
import java.util.UUID;

public interface ProjectAllocationEntityRepository extends CrudRepository<ProjectAllocationEntity, UUID> {
	boolean existsByCommunityAllocationId(UUID communityAllocationId);
	long countByProjectId(UUID projectId);
	Set<ProjectAllocationEntity> findAllByProjectId(UUID projectId);
}
