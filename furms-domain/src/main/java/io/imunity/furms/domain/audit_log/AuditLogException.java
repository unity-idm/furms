/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.domain.audit_log;

public class AuditLogException extends RuntimeException {
	public AuditLogException() {
	}

	public AuditLogException(String message, Throwable cause) {
		super(message, cause);
	}
}
