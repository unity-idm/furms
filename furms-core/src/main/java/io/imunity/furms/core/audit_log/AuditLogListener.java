/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.core.audit_log;

import io.imunity.furms.domain.audit_log.AuditLogEvent;

interface AuditLogListener {
	void onAuditLogEvent(AuditLogEvent event);
}
