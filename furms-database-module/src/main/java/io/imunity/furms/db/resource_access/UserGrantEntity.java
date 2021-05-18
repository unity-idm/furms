/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.db.resource_access;

import io.imunity.furms.db.id.uuid.UUIDIdentifiable;
import org.springframework.data.relational.core.mapping.Table;

import java.util.Objects;
import java.util.UUID;

@Table("user_grant")
class UserGrantEntity extends UUIDIdentifiable {
	public final UUID siteId;
	public final UUID projectId;
	public final UUID projectAllocationId;
	public final String userId;

	UserGrantEntity(UUID id, UUID siteId, UUID projectId, UUID projectAllocationId, String userId) {
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
		UserGrantEntity that = (UserGrantEntity) o;
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

	public static UserGrantEntityBuilder builder() {
		return new UserGrantEntityBuilder();
	}

	public static final class UserGrantEntityBuilder {
		private UUID id;
		private UUID siteId;
		private UUID projectId;
		private UUID projectAllocationId;
		private String userId;

		private UserGrantEntityBuilder() {
		}

		public UserGrantEntityBuilder id(UUID id) {
			this.id = id;
			return this;
		}

		public UserGrantEntityBuilder siteId(UUID siteId) {
			this.siteId = siteId;
			return this;
		}

		public UserGrantEntityBuilder projectId(UUID projectId) {
			this.projectId = projectId;
			return this;
		}

		public UserGrantEntityBuilder projectAllocationId(UUID projectAllocationId) {
			this.projectAllocationId = projectAllocationId;
			return this;
		}

		public UserGrantEntityBuilder userId(String userId) {
			this.userId = userId;
			return this;
		}

		public UserGrantEntity build() {
			return new UserGrantEntity(id, siteId, projectId, projectAllocationId, userId);
		}
	}
}
