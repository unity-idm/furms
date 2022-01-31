/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.api.audit_log;

import io.imunity.furms.domain.audit_log.AuditLog;
import io.imunity.furms.domain.users.FURMSUser;

import java.time.ZonedDateTime;
import java.util.Set;

public interface AuditLogService {
	Set<AuditLog> findBy(ZonedDateTime from, ZonedDateTime to, Set<FURMSUser> originators, Set<Integer> actionIds, Set<Integer> operationIds);
}
