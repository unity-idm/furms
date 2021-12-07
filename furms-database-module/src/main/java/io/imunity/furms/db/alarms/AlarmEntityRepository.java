/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.db.alarms;

import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

interface AlarmEntityRepository extends CrudRepository<AlarmEntity, UUID> {
	Set<AlarmEntity> findAllByProjectId(UUID projectId);
	Optional<AlarmEntity> findByProjectAllocationId(UUID projectAllocationId);
	boolean existsByIdAndProjectId(UUID id, UUID projectId);
	boolean existsByProjectIdAndName(UUID projectId, String name);

	@Query("SELECT a.*, pa.name AS project_allocation_name " +
		"FROM alarm_configuration a " +
		"LEFT JOIN alarm_configuration_user au ON a.id = au.alarm_id " +
		"JOIN project_allocation pa ON a.project_allocation_id = pa.id " +
		"WHERE a.fired AND (au.user_id = :user_id OR (a.all_users AND (a.project_id IN (:project_ids))))")
	Set<ExtendedAlarmEntity> findAllFiredByProjectIdsOrUserId(@Param("project_ids") List<UUID> projectIds, @Param("user_id") String userId);

	@Query("SELECT a.*, pa.name AS project_allocation_name " +
		"FROM alarm_configuration a " +
		"LEFT JOIN alarm_configuration_user au ON a.id = au.alarm_id " +
		"JOIN project_allocation pa ON a.project_allocation_id = pa.id " +
		"WHERE a.fired AND au.user_id = :user_id")
	Set<ExtendedAlarmEntity> findAllFiredByUserId(@Param("user_id") String userId);
}
