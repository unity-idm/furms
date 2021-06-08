/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.db.resource_usage;

import org.springframework.data.repository.CrudRepository;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public interface ResourceUsageEntityRepository extends CrudRepository<ResourceUsageEntity, UUID> {
	Optional<ResourceUsageEntity> findByProjectAllocationId(UUID projectAllocationId);
	Set<ResourceUsageEntity> findAllByProjectId(UUID projectId);
}