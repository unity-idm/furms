/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.db.user_operation;

import io.imunity.furms.db.id.uuid.UUIDIdentifiable;
import org.springframework.data.relational.core.mapping.Table;

import java.util.Objects;
import java.util.UUID;

@Table("user_addition")
public class UserAdditionSaveEntity extends UUIDIdentifiable {

	public final UUID siteId;
	public final UUID projectId;
	public final String uid;
	public final String userId;

	UserAdditionSaveEntity(UUID id, UUID siteId, UUID projectId, String uid, String userId) {
		this.id = id;
		this.siteId = siteId;
		this.projectId = projectId;
		this.uid = uid;
		this.userId = userId;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		UserAdditionSaveEntity that = (UserAdditionSaveEntity) o;
		return Objects.equals(projectId, that.projectId) &&
			Objects.equals(siteId, that.siteId) &&
			Objects.equals(uid, that.uid) &&
			Objects.equals(userId, that.userId);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, projectId, siteId, userId);
	}

	@Override
	public String toString() {
		return "UserAdditionSaveEntity{" +
			"id=" + id +
			", siteId=" + siteId +
			", projectId=" + projectId +
			", uid=" + uid +
			", userId='" + userId + '\'' +
			'}';
	}

	public static UserAdditionEntityBuilder builder() {
		return new UserAdditionEntityBuilder();
	}

	public static final class UserAdditionEntityBuilder {
		protected UUID id;
		public UUID siteId;
		public UUID projectId;
		public String uid;
		public String userId;

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

		public UserAdditionEntityBuilder userId(String userId) {
			this.userId = userId;
			return this;
		}

		public UserAdditionEntityBuilder uid(String uid) {
			this.uid = uid;
			return this;
		}

		public UserAdditionSaveEntity build() {
			return new UserAdditionSaveEntity(id, siteId, projectId, uid, userId);
		}
	}
}
