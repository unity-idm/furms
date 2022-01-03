/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.domain.audit_log;

import io.imunity.furms.domain.users.FURMSUser;

import java.time.LocalDateTime;
import java.util.Objects;

public class AuditLog {
	public final LocalDateTime utcTimestamp;
	public final FURMSUser originator;
	public final Operation operationCategory;
	public final Action action;
	public final String operationSubject;
	public final String dataJson;

	private AuditLog(LocalDateTime utcTimestamp, FURMSUser originator, Operation operationCategory, Action action, String operationSubject, String dataJson) {
		this.utcTimestamp = utcTimestamp;
		this.originator = originator;
		this.operationCategory = operationCategory;
		this.action = action;
		this.operationSubject = operationSubject;
		this.dataJson = dataJson;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		AuditLog auditLog = (AuditLog) o;
		return Objects.equals(utcTimestamp, auditLog.utcTimestamp) &&
			Objects.equals(originator, auditLog.originator) &&
			operationCategory == auditLog.operationCategory &&
			action == auditLog.action &&
			Objects.equals(operationSubject, auditLog.operationSubject) &&
			Objects.equals(dataJson, auditLog.dataJson);
	}

	@Override
	public int hashCode() {
		return Objects.hash(utcTimestamp, originator, operationCategory, action, operationSubject, dataJson);
	}

	@Override
	public String toString() {
		return "AuditLog{" +
			"utcTimestamp=" + utcTimestamp +
			", originator=" + originator +
			", operationCategory=" + operationCategory +
			", action=" + action +
			", operationSubject='" + operationSubject + '\'' +
			", dataJson='" + dataJson + '\'' +
			'}';
	}

	public static AuditLogBuilder builder() {
		return new AuditLogBuilder();
	}

	public static final class AuditLogBuilder {
		private LocalDateTime utcTimestamp;
		private FURMSUser originator;
		private Operation operationCategory;
		private Action action;
		private String operationSubject;
		private String dataJson;

		private AuditLogBuilder() {
		}

		public AuditLogBuilder utcTimestamp(LocalDateTime utcTimestamp) {
			this.utcTimestamp = utcTimestamp;
			return this;
		}

		public AuditLogBuilder originator(FURMSUser originator) {
			this.originator = originator;
			return this;
		}

		public AuditLogBuilder operationCategory(Operation operationCategory) {
			this.operationCategory = operationCategory;
			return this;
		}

		public AuditLogBuilder action(Action action) {
			this.action = action;
			return this;
		}

		public AuditLogBuilder operationSubject(String operationSubject) {
			this.operationSubject = operationSubject;
			return this;
		}

		public AuditLogBuilder operationSubject(FURMSUser furmsUser) {
			this.operationSubject = furmsUser.firstName.orElse("") + " " + furmsUser.lastName.orElse("") + " " + furmsUser.email;
			return this;
		}

		public AuditLogBuilder dataJson(String dataJson) {
			this.dataJson = dataJson;
			return this;
		}

		public AuditLog build() {
			return new AuditLog(utcTimestamp, originator, operationCategory, action, operationSubject, dataJson);
		}
	}
}
