/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.db.audit_log;

import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

public interface AuditLogEntityRepository extends CrudRepository<AuditLogEntity, UUID> {
	@Query(
		"SELECT * " +
		"FROM audit_log al " +
		"WHERE (al.originator_id IN (:originator_ids) OR al.originator_persistent_id IN (:originator_persistent_ids)) " +
		"AND al.operation_action IN (:action_ids) AND al.operation_category IN (:operation_ids) " +
		"AND al.creation_time BETWEEN :from AND :to")
	Set<AuditLogEntity> findByCreationTimeBetweenAndOperationActionInAndOperationCategoryInAndOriginatorIdInOrOriginatorPersistentIdIn(
		@Param("from") LocalDateTime from,
		@Param("to") LocalDateTime to,
		@Param("action_ids") Set<Integer> actionIds,
		@Param("operation_ids") Set<Integer> operationIds,
		@Param("originator_ids") Set<String> originatorIds,
		@Param("originator_persistent_ids") Set<String> originatorPersistentIds
	);

	Set<AuditLogEntity> findByCreationTimeBetweenAndOperationActionInAndOperationCategoryIn(
		LocalDateTime from,
		LocalDateTime to,
		Set<Integer> actionIds,
		Set<Integer> operationIds
	);

	Set<AuditLogEntity> findByCreationTimeBetweenAndOperationActionInAndOperationCategoryInAndOriginatorIdIn(
		LocalDateTime from,
		LocalDateTime to,
		Set<Integer> actionIds,
		Set<Integer> operationIds,
		Set<String> originatorIds
	);

	Set<AuditLogEntity> findByCreationTimeBetweenAndOperationActionInAndOperationCategoryInAndOriginatorPersistentIdIn(
		LocalDateTime from,
		LocalDateTime to,
		Set<Integer> actionIds,
		Set<Integer> operationIds,
		Set<String> originatorIds
	);
}
