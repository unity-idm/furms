/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.db.user_operation;

import io.imunity.furms.db.id.uuid.UUIDIdentifiable;
import io.imunity.furms.domain.user_operation.UserRemovalStatus;
import org.springframework.data.relational.core.mapping.Table;

import java.util.Objects;
import java.util.UUID;

@Table("user_removal")
public class UserRemovalSaveEntity extends UUIDIdentifiable {

	public final UUID siteId;
	public final UUID projectId;
	public final UUID correlationId;
	public final UUID userAdditionId;
	public final String userId;
	public final int status;

	UserRemovalSaveEntity(UUID id, UUID siteId, UUID projectId, UUID correlationId, UUID userAdditionId, String userId, int status) {
		this.id = id;
		this.siteId = siteId;
		this.projectId = projectId;
		this.correlationId = correlationId;
		this.userAdditionId = userAdditionId;
		this.userId = userId;
		this.status = status;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		UserRemovalSaveEntity that = (UserRemovalSaveEntity) o;
		return Objects.equals(projectId, that.projectId) &&
			Objects.equals(siteId, that.siteId) &&
			Objects.equals(correlationId, that.correlationId) &&
			Objects.equals(userAdditionId, that.userAdditionId) &&
			Objects.equals(userId, that.userId);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, projectId, siteId, correlationId, userAdditionId, userId, status);
	}

	@Override
	public String toString() {
		return "UserRemovalSaveEntity{" +
			"id=" + id +
			", siteId=" + siteId +
			", projectId=" + projectId +
			", correlationId=" + correlationId +
			", userAdditionalId=" + userAdditionId +
			", userId='" + userId + '\'' +
			", status=" + status +
			'}';
	}

	public static UserAdditionEntityBuilder builder() {
		return new UserAdditionEntityBuilder();
	}

	public static final class UserAdditionEntityBuilder {
		public UUID siteId;
		public UUID projectId;
		public UUID correlationId;
		public UUID userAdditionId;
		public String userId;
		public String  uid;
		public int status;
		protected UUID id;

		private UserAdditionEntityBuilder() {
		}

		public UserAdditionEntityBuilder id(UUID id) {
			this.id = id;
			return this;
		}

		public UserAdditionEntityBuilder siteId(UUID siteId) {
			this.siteId = siteId;
			return this;
		}

		public UserAdditionEntityBuilder projectId(UUID projectId) {
			this.projectId = projectId;
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

		public UserAdditionEntityBuilder userId(String userId) {
			this.userId = userId;
			return this;
		}

		public UserAdditionEntityBuilder uid(String uid) {
			this.uid = uid;
			return this;
		}

		public UserAdditionEntityBuilder status(UserRemovalStatus status) {
			this.status = status.getPersistentId();
			return this;
		}

		public UserRemovalSaveEntity build() {
			return new UserRemovalSaveEntity(id, siteId, projectId, correlationId, userAdditionId, userId, status);
		}
	}
}
