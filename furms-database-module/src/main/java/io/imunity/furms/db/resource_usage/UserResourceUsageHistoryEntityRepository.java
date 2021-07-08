/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.db.resource_usage;

import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

public interface UserResourceUsageHistoryEntityRepository extends CrudRepository<UserResourceUsageHistoryEntity, Long> {
	Set<UserResourceUsageHistoryEntity> findAllByProjectAllocationId(UUID projectAllocationId);

	@Query("SELECT * " +
			"FROM user_resource_usage_history uruh " +
			"WHERE uruh.project_allocation_id IN :allocations")
	Set<UserResourceUsageHistoryEntity> findAllByProjectAllocationIdIn(@Param("allocations") Set<UUID> projectAllocations);
}
