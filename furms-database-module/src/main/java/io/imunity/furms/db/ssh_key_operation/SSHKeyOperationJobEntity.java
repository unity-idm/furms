/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.db.ssh_key_operation;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

import org.springframework.data.relational.core.mapping.Table;

import io.imunity.furms.db.id.uuid.UUIDIdentifiable;
import io.imunity.furms.domain.ssh_keys.SSHKeyOperation;
import io.imunity.furms.domain.ssh_keys.SSHKeyOperationStatus;

@Table("ssh_key_operation_job")
public class SSHKeyOperationJobEntity extends UUIDIdentifiable {
	public final UUID correlationId;
	public final UUID siteId;
	public final UUID sshkeyId;
	public final SSHKeyOperation operation;
	public final SSHKeyOperationStatus status;
	public final String error;
	public final LocalDateTime originationTime;

	SSHKeyOperationJobEntity(UUID id, UUID correlationId, UUID siteId, UUID sshkeyId, SSHKeyOperation operation,
			SSHKeyOperationStatus status, String error, LocalDateTime originationTime) {
		this.id = id;
		this.correlationId = correlationId;
		this.siteId = siteId;
		this.sshkeyId = sshkeyId;
		this.operation = operation;
		this.status = status;
		this.error = error;
		this.originationTime = originationTime;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		SSHKeyOperationJobEntity that = (SSHKeyOperationJobEntity) o;
		return Objects.equals(id, that.id) && Objects.equals(correlationId, that.correlationId)
				&& Objects.equals(siteId, that.siteId) && Objects.equals(sshkeyId, that.sshkeyId)
				&& operation == that.operation && status == that.status
				&& Objects.equals(error, that.error)
				&& Objects.equals(originationTime, that.originationTime);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, correlationId, siteId, sshkeyId, operation, status, error, operation);
	}

	@Override
	public String toString() {
		return "SSHKeyInstallationJobEntity{" + "id=" + id + ", correlationId=" + correlationId + ", siteId="
				+ siteId + ", sshkeyId=" + sshkeyId + ", status=" + status + ", operation=" + operation
				+ ", error=" + error + ", originationTime=" + originationTime + '}';
	}

	public static SSHKeyInstallationJobEntityBuilder builder() {
		return new SSHKeyInstallationJobEntityBuilder();
	}

	public static final class SSHKeyInstallationJobEntityBuilder {
		public UUID correlationId;
		public UUID siteId;
		public UUID sshkeyId;
		public SSHKeyOperation operation;
		public SSHKeyOperationStatus status;
		public UUID id;
		public String error;
		private LocalDateTime originationTime;

		private SSHKeyInstallationJobEntityBuilder() {
		}

		public SSHKeyInstallationJobEntityBuilder id(UUID id) {
			this.id = id;
			return this;
		}

		public SSHKeyInstallationJobEntityBuilder correlationId(UUID correlationId) {
			this.correlationId = correlationId;
			return this;
		}

		public SSHKeyInstallationJobEntityBuilder siteId(UUID siteId) {
			this.siteId = siteId;
			return this;
		}

		public SSHKeyInstallationJobEntityBuilder sshkeyId(UUID sshkeyId) {
			this.sshkeyId = sshkeyId;
			return this;
		}

		public SSHKeyInstallationJobEntityBuilder operation(SSHKeyOperation operation) {
			this.operation = operation;
			return this;
		}

		public SSHKeyInstallationJobEntityBuilder status(SSHKeyOperationStatus status) {
			this.status = status;
			return this;
		}

		public SSHKeyInstallationJobEntityBuilder error(String error) {
			this.error = error;
			return this;
		}

		public SSHKeyInstallationJobEntityBuilder originationTime(LocalDateTime originationTime) {
			this.originationTime = originationTime;
			return this;
		}

		public SSHKeyOperationJobEntity build() {
			return new SSHKeyOperationJobEntity(id, correlationId, siteId, sshkeyId, operation, status,
					error, originationTime);
		}
	}
}
