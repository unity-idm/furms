/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.domain.audit_log;

import io.imunity.furms.domain.FurmsEvent;

import java.util.Objects;

public class AuditLogEvent implements FurmsEvent {
	public final AuditLog auditLog;

	public AuditLogEvent(AuditLog auditLog) {
		this.auditLog = auditLog;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		AuditLogEvent that = (AuditLogEvent) o;
		return Objects.equals(auditLog, that.auditLog);
	}

	@Override
	public int hashCode() {
		return Objects.hash(auditLog);
	}

	@Override
	public String toString() {
		return "AuditLogEvent{" +
			"auditLog=" + auditLog +
			'}';
	}
}
