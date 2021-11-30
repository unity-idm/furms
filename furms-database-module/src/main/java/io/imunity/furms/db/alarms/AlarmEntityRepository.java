/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.db.alarms;

import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

interface AlarmEntityRepository extends CrudRepository<AlarmEntity, UUID> {
	Set<AlarmEntity> findAllByProjectId(UUID projectId);
	Optional<AlarmEntity> findByProjectAllocationId(UUID projectAllocationId);
	boolean existsByIdAndProjectId(UUID id, UUID projectId);
	boolean existsByProjectIdAndName(UUID projectId, String name);

	@Query("SELECT a.*, pa.name AS project_allocation_name, pa.amount AS allocation_amount, ru.cumulative_consumption AS cumulative_consumption " +
		"FROM alarm a " +
		"LEFT JOIN alarm_user au ON a.id = au.alarm_id " +
		"JOIN project_allocation pa ON a.project_allocation_id = pa.id " +
		"JOIN resource_usage ru ON a.project_allocation_id = ru.project_allocation_id " +
		"WHERE au.user_id = :user_id OR a.all_users")
	Set<ExtendedAlarmEntity> findAllByUserId(@Param("user_id") String userId);
}
