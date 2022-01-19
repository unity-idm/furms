/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.db.audit_log;

import org.springframework.data.repository.CrudRepository;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

public interface AuditLogEntityRepository extends CrudRepository<AuditLogEntity, UUID> {
	Set<AuditLogEntity> findByCreationTimeBetweenAndOperationActionInAndOperationCategoryInAndOperationSubjectContainingAndOriginatorIdInOrOriginatorPersistentIdIn(
		LocalDateTime from,
		LocalDateTime to,
		Set<Integer> actionIds,
		Set<Integer> operationIds,
		String operationSubject,
		Set<String> originatorIds,
		Set<String> originatorPersistenceIds
	);
}
