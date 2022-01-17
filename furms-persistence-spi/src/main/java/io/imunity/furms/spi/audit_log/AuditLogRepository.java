/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.spi.audit_log;

import io.imunity.furms.domain.audit_log.AuditLog;
import io.imunity.furms.domain.users.FURMSUser;

import java.time.LocalDateTime;
import java.util.Set;

public interface AuditLogRepository {
	Set<AuditLog> findBy(LocalDateTime from, LocalDateTime to, Set<FURMSUser> originatorIds, Set<Integer> actionIds, Set<Integer> operationIds, String subject);
	void create(AuditLog auditLog);
}
