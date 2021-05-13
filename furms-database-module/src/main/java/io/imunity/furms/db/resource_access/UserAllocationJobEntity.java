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

@Table("user_allocation_job")
class UserAllocationJobEntity extends UUIDIdentifiable {
	public final UUID correlationId;
	public final UUID userAllocationId;
	public final int status;
	public final String message;

	UserAllocationJobEntity(UUID id, UUID correlationId, UUID userAllocationId, int status, String message) {
		this.id = id;
		this.correlationId = correlationId;
		this.userAllocationId = userAllocationId;
		this.status = status;
		this.message = message;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		UserAllocationJobEntity that = (UserAllocationJobEntity) o;
		return status == that.status &&
			Objects.equals(id, that.id) &&
			Objects.equals(correlationId, that.correlationId) &&
			Objects.equals(userAllocationId, that.userAllocationId) &&
			Objects.equals(message, that.message);
	}

	@Override
	public int hashCode() {
		return Objects.hash(correlationId, userAllocationId, status, message);
	}

	@Override
	public String toString() {
		return "ResourceRevokeEntity{" +
			"id=" + id +
			", correlationId=" + correlationId +
			", userAllocationId=" + userAllocationId +
			", status=" + status +
			", message='" + message + '\'' +
			'}';
	}

	public static UserAllocationJobEntityBuilder builder() {
		return new UserAllocationJobEntityBuilder();
	}

	public static final class UserAllocationJobEntityBuilder {
		private UUID id;
		private UUID correlationId;
		private UUID userAllocationId;
		private int status;
		private String message;

		private UserAllocationJobEntityBuilder() {
		}

		public UserAllocationJobEntityBuilder id(UUID id) {
			this.id = id;
			return this;
		}

		public UserAllocationJobEntityBuilder correlationId(UUID correlationId) {
			this.correlationId = correlationId;
			return this;
		}

		public UserAllocationJobEntityBuilder userAllocationId(UUID userAllocationId) {
			this.userAllocationId = userAllocationId;
			return this;
		}

		public UserAllocationJobEntityBuilder status(AccessStatus status) {
			this.status = status.getPersistentId();
			return this;
		}

		public UserAllocationJobEntityBuilder message(String message) {
			this.message = message;
			return this;
		}

		public UserAllocationJobEntity build() {
			return new UserAllocationJobEntity(id, correlationId, userAllocationId, status, message);
		}
	}
}
