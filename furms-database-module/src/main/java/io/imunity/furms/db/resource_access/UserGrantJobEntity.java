/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.db.resource_access;

import io.imunity.furms.db.id.uuid.UUIDIdentifiable;
import io.imunity.furms.domain.resource_access.AccessStatus;
import org.springframework.data.relational.core.mapping.Table;

import java.util.Objects;
import java.util.UUID;

@Table("user_grant_job")
class UserGrantJobEntity extends UUIDIdentifiable {
	public final UUID correlationId;
	public final UUID userGrantId;
	public final int status;
	public final String message;

	UserGrantJobEntity(UUID id, UUID correlationId, UUID userGrantId, int status, String message) {
		this.id = id;
		this.correlationId = correlationId;
		this.userGrantId = userGrantId;
		this.status = status;
		this.message = message;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		UserGrantJobEntity that = (UserGrantJobEntity) o;
		return status == that.status &&
			Objects.equals(id, that.id) &&
			Objects.equals(correlationId, that.correlationId) &&
			Objects.equals(userGrantId, that.userGrantId) &&
			Objects.equals(message, that.message);
	}

	@Override
	public int hashCode() {
		return Objects.hash(correlationId, userGrantId, status, message);
	}

	@Override
	public String toString() {
		return "ResourceRevokeEntity{" +
			"id=" + id +
			", correlationId=" + correlationId +
			", userAllocationId=" + userGrantId +
			", status=" + status +
			", message='" + message + '\'' +
			'}';
	}

	public static UserGrantJobEntityBuilder builder() {
		return new UserGrantJobEntityBuilder();
	}

	public static final class UserGrantJobEntityBuilder {
		private UUID id;
		private UUID correlationId;
		private UUID userAllocationId;
		private int status;
		private String message;

		private UserGrantJobEntityBuilder() {
		}

		public UserGrantJobEntityBuilder id(UUID id) {
			this.id = id;
			return this;
		}

		public UserGrantJobEntityBuilder correlationId(UUID correlationId) {
			this.correlationId = correlationId;
			return this;
		}

		public UserGrantJobEntityBuilder userAllocationId(UUID userAllocationId) {
			this.userAllocationId = userAllocationId;
			return this;
		}

		public UserGrantJobEntityBuilder status(AccessStatus status) {
			this.status = status.getPersistentId();
			return this;
		}

		public UserGrantJobEntityBuilder message(String message) {
			this.message = message;
			return this;
		}

		public UserGrantJobEntity build() {
			return new UserGrantJobEntity(id, correlationId, userAllocationId, status, message);
		}
	}
}
