/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.db.resource_usage;

import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public interface ResourceUsageEntityRepository extends CrudRepository<ResourceUsageEntity, UUID> {
	@Query(
		"SELECT ru.* " +
		"FROM (" +
			"SELECT project_allocation_id, MAX(probed_at) as newest_timestamp " +
			"FROM resource_usage " +
			"WHERE project_id = :project_id " +
			"GROUP BY project_allocation_id" +
		") ru_pom " +
		"INNER JOIN resource_usage ru ON " +
			"ru.project_allocation_id = ru_pom.project_allocation_id AND " +
			"ru.probed_at = ru_pom.newest_timestamp"
	)
	Set<ResourceUsageEntity> findAllNewestByProjectId(@Param("project_id") UUID projectId);

	@Query(
		"SELECT ru.* " +
			"FROM (" +
			"SELECT project_allocation_id, MAX(probed_at) as newest_timestamp " +
			"FROM resource_usage " +
			"WHERE project_allocation_id = :project_allocation_id " +
			"GROUP BY project_allocation_id" +
			") ru_pom " +
			"INNER JOIN resource_usage ru ON " +
			"ru.project_allocation_id = ru_pom.project_allocation_id AND " +
			"ru.probed_at = ru_pom.newest_timestamp"
	)
	Optional<ResourceUsageEntity> findNewestByProjectAllocationId(@Param("project_allocation_id") UUID projectAllocationId);
}
