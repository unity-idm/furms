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
	public final String resourceId;
	public final LocalDateTime creationTime;
	public final String originatorPersistentId;
	public final String originatorId;
	public final int operationCategory;
	public final int operationAction;
	public final String operationSubject;
	public final String dataJson;

	AuditLogEntity(UUID id, String resourceId, LocalDateTime creationTime, String originatorId, String originatorPersistentId, int operationCategory, int operationAction, String operationSubject, String dataJson) {
		this.id = id;
		this.resourceId = resourceId;
		this.creationTime = creationTime;
		this.originatorPersistentId = originatorPersistentId;
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
			Objects.equals(resourceId, that.resourceId) &&
			Objects.equals(originatorPersistentId, that.originatorPersistentId) &&
			Objects.equals(originatorId, that.originatorId) &&
			Objects.equals(creationTime, that.creationTime) &&
			Objects.equals(operationSubject, that.operationSubject) &&
			Objects.equals(dataJson, that.dataJson);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, resourceId, creationTime, originatorId, originatorPersistentId, operationCategory, operationAction, operationSubject, dataJson);
	}

	@Override
	public String toString() {
		return "AuditLogEntity{" +
			"originatorId='" + originatorId + '\'' +
			",originatorPersistenceId='" + originatorPersistentId + '\'' +
			", resourceId=" + resourceId +
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
		private String resourceId;
		private String originatorId;
		private String originatorPersistentId;
		private int operationCategory;
		private int operationAction;
		private String operationSubject;
		private String dataJson;

		private AuditLogEntityBuilder() {
		}

		public AuditLogEntityBuilder resourceId(String resourceId) {
			this.resourceId = resourceId;
			return this;
		}

		public AuditLogEntityBuilder originatorId(String originatorId) {
			this.originatorId = originatorId;
			return this;
		}

		public AuditLogEntityBuilder originatorPersistenceId(String originatorPersistentId) {
			this.originatorPersistentId = originatorPersistentId;
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
			return new AuditLogEntity(null, resourceId, creationTime, originatorId, originatorPersistentId, operationCategory, operationAction, operationSubject, dataJson);
		}
	}
}
