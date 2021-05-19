/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.db.user_operation;

import io.imunity.furms.db.id.uuid.UUIDIdentifiable;
import io.imunity.furms.domain.user_operation.UserStatus;
import org.springframework.data.relational.core.mapping.Table;

import java.util.Objects;
import java.util.UUID;

@Table("user_addition_job")
public class UserAdditionJobEntity extends UUIDIdentifiable {

	public final UUID userAdditionId;
	public final UUID correlationId;
	public final int status;
	public final String code;
	public final String message;

	UserAdditionJobEntity(UUID id, UUID correlationId, UUID userAdditionId, int status, String code, String message) {
		this.id = id;
		this.correlationId = correlationId;
		this.userAdditionId = userAdditionId;
		this.status = status;
		this.code = code;
		this.message = message;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		UserAdditionJobEntity that = (UserAdditionJobEntity) o;
		return Objects.equals(correlationId, that.correlationId) &&
			Objects.equals(userAdditionId, that.userAdditionId) &&
			Objects.equals(status, that.status) &&
			Objects.equals(code, that.code) &&
			Objects.equals(message, that.message);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, message, correlationId, userAdditionId, status, code);
	}

	@Override
	public String toString() {
		return "UserAdditionJobEntity{" +
			"id=" + id +
			", correlationId=" + correlationId +
			", userAdditionalId=" + userAdditionId +
			", status=" + status +
			", code=" + code +
			", message=" + message +
			'}';
	}

	public static UserAdditionEntityBuilder builder() {
		return new UserAdditionEntityBuilder();
	}

	public static final class UserAdditionEntityBuilder {
		protected UUID id;
		public UUID correlationId;
		public UUID userAdditionId;
		public int status;
		public String code;
		public String message;

		private UserAdditionEntityBuilder() {
		}

		public UserAdditionEntityBuilder id(UUID id) {
			this.id = id;
			return this;
		}

		public UserAdditionEntityBuilder userAdditionId(UUID userAdditionalId) {
			this.userAdditionId = userAdditionalId;
			return this;
		}

		public UserAdditionEntityBuilder correlationId(UUID correlationId) {
			this.correlationId = correlationId;
			return this;
		}

		public UserAdditionEntityBuilder message(String message) {
			this.message = message;
			return this;
		}

		public UserAdditionEntityBuilder code(String code) {
			this.code = code;
			return this;
		}

		public UserAdditionEntityBuilder status(UserStatus status) {
			this.status = status.getPersistentId();
			return this;
		}

		public UserAdditionJobEntity build() {
			return new UserAdditionJobEntity(id, correlationId, userAdditionId, status, code, message);
		}
	}
}
