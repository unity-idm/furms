/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.db.resource_access;

import io.imunity.furms.db.id.uuid.UUIDIdentifiable;
import org.springframework.data.relational.core.mapping.Table;

import java.util.Objects;
import java.util.UUID;

@Table("user_allocation")
class UserAllocationEntity extends UUIDIdentifiable {
	public final UUID siteId;
	public final UUID projectId;
	public final UUID projectAllocationId;
	public final String userId;

	UserAllocationEntity(UUID id, UUID siteId, UUID projectId, UUID projectAllocationId, String userId) {
		this.id = id;
		this.siteId = siteId;
		this.projectId = projectId;
		this.projectAllocationId = projectAllocationId;
		this.userId = userId;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		UserAllocationEntity that = (UserAllocationEntity) o;
		return Objects.equals(id, that.id) &&
			Objects.equals(siteId, that.siteId) &&
			Objects.equals(projectId, that.projectId) &&
			Objects.equals(projectAllocationId, that.projectAllocationId) &&
			Objects.equals(userId, that.userId);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, siteId, projectId, projectAllocationId, userId);
	}

	@Override
	public String toString() {
		return "UserAllocationEntity{" +
			"id=" + id +
			", siteId=" + siteId +
			", projectId=" + projectId +
			", projectAllocationId=" + projectAllocationId +
			", userId='" + userId + '\'' +
			'}';
	}

	public static UserAllocationEntityBuilder builder() {
		return new UserAllocationEntityBuilder();
	}

	public static final class UserAllocationEntityBuilder {
		private UUID id;
		private UUID siteId;
		private UUID projectId;
		private UUID projectAllocationId;
		private String userId;

		private UserAllocationEntityBuilder() {
		}

		public UserAllocationEntityBuilder id(UUID id) {
			this.id = id;
			return this;
		}

		public UserAllocationEntityBuilder siteId(UUID siteId) {
			this.siteId = siteId;
			return this;
		}

		public UserAllocationEntityBuilder projectId(UUID projectId) {
			this.projectId = projectId;
			return this;
		}

		public UserAllocationEntityBuilder projectAllocationId(UUID projectAllocationId) {
			this.projectAllocationId = projectAllocationId;
			return this;
		}

		public UserAllocationEntityBuilder userId(String userId) {
			this.userId = userId;
			return this;
		}

		public UserAllocationEntity build() {
			return new UserAllocationEntity(id, siteId, projectId, projectAllocationId, userId);
		}
	}
}
