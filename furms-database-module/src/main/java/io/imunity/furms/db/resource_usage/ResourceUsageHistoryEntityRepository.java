/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.db.resource_usage;

import org.springframework.data.repository.CrudRepository;

import java.util.Set;
import java.util.UUID;

public interface ResourceUsageHistoryEntityRepository extends CrudRepository<ResourceUsageHistoryEntity, Long> {
	Set<ResourceUsageHistoryEntity> findAllByProjectAllocationId(UUID projectAllocationId);
	Set<ResourceUsageHistoryEntity> findAllByProjectId(UUID projectId);
}
