/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.db.audit_log;

import io.imunity.furms.db.id.uuid.UUIDIdentifiable;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

@Table("audit_log")
class AuditLogEntity extends UUIDIdentifiable {
	public final LocalDateTime creationTime;
	public final String originatorPersistenceId;
	public final String originatorId;
	public final int operationCategory;
	public final int operationAction;
	public final String operationSubject;
	public final String dataJson;

	AuditLogEntity(UUID id, LocalDateTime creationTime, String originatorId, String originatorPersistenceId, int operationCategory, int operationAction, String operationSubject, String dataJson) {
		this.id = id;
		this.creationTime = creationTime;
		this.originatorPersistenceId = originatorPersistenceId;
		this.originatorId = originatorId;
		this.operationCategory = operationCategory;
		this.operationAction = operationAction;
		this.operationSubject = operationSubject;
		this.dataJson = dataJson;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		AuditLogEntity that = (AuditLogEntity) o;
		return operationCategory == that.operationCategory &&
			operationAction == that.operationAction &&
			Objects.equals(id, that.id) &&
			Objects.equals(originatorPersistenceId, that.originatorPersistenceId) &&
			Objects.equals(originatorId, that.originatorId) &&
			Objects.equals(creationTime, that.creationTime) &&
			Objects.equals(operationSubject, that.operationSubject) &&
			Objects.equals(dataJson, that.dataJson);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, creationTime, originatorId, originatorPersistenceId, operationCategory, operationAction, operationSubject, dataJson);
	}

	@Override
	public String toString() {
		return "AuditLogEntity{" +
			"originatorId='" + originatorId + '\'' +
			"originatorPersistenceId='" + originatorPersistenceId + '\'' +
			", operationCategory=" + operationCategory +
			", operationAction=" + operationAction +
			", operationSubject='" + operationSubject + '\'' +
			", timestamp='" + creationTime + '\'' +
			", dataJson='" + dataJson + '\'' +
			", id=" + id +
			'}';
	}

	public static AuditLogEntityBuilder builder() {
		return new AuditLogEntityBuilder();
	}

	public static final class AuditLogEntityBuilder {
		private LocalDateTime creationTime;
		private String originatorId;
		private String originatorPersistenceId;
		private int operationCategory;
		private int operationAction;
		private String operationSubject;
		private String dataJson;

		private AuditLogEntityBuilder() {
		}

		public AuditLogEntityBuilder originatorId(String originatorId) {
			this.originatorId = originatorId;
			return this;
		}

		public AuditLogEntityBuilder originatorPersistenceId(String originatorPersistenceId) {
			this.originatorId = originatorId;
			return this;
		}

		public AuditLogEntityBuilder operationCategory(int operationCategory) {
			this.operationCategory = operationCategory;
			return this;
		}

		public AuditLogEntityBuilder operationAction(int operationAction) {
			this.operationAction = operationAction;
			return this;
		}

		public AuditLogEntityBuilder operationSubject(String operationSubject) {
			this.operationSubject = operationSubject;
			return this;
		}

		public AuditLogEntityBuilder dataJson(String dataJson) {
			this.dataJson = dataJson;
			return this;
		}

		public AuditLogEntityBuilder creationTime(LocalDateTime creationTime) {
			this.creationTime = creationTime;
			return this;
		}

		public AuditLogEntity build() {
			return new AuditLogEntity(null, creationTime, originatorId, originatorPersistenceId, operationCategory, operationAction, operationSubject, dataJson);
		}
	}
}
