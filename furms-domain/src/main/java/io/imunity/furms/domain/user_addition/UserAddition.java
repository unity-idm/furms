/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.domain.user_addition;

import java.util.Objects;

public class UserAddition {
	public final String id;
	public final String siteId;
	public final String projectId;
	public final String correlationId;
	public final String userId;
	public final String  uid;
	public final UserAdditionStatus status;

	UserAddition(String id, String siteId, String projectId, String correlationId, String userId, String uid, UserAdditionStatus status) {
		this.id = id;
		this.siteId = siteId;
		this.projectId = projectId;
		this.correlationId = correlationId;
		this.userId = userId;
		this.uid = uid;
		this.status = status;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		UserAddition that = (UserAddition) o;
		return Objects.equals(id, that.id) &&
			Objects.equals(siteId, that.siteId) &&
			Objects.equals(projectId, that.projectId) &&
			Objects.equals(correlationId, that.correlationId) &&
			Objects.equals(userId, that.userId) &&
			Objects.equals(uid, that.uid) && status == that.status;
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, siteId, projectId, correlationId, userId, uid, status);
	}

	@Override
	public String toString() {
		return "UserAddition{" +
			"id='" + id + '\'' +
			", siteId='" + siteId + '\'' +
			", projectId='" + projectId + '\'' +
			", correlationId='" + correlationId + '\'' +
			", userId='" + userId + '\'' +
			", uid='" + uid + '\'' +
			", status=" + status +
			'}';
	}

	public static UserAdditionBuilder builder() {
		return new UserAdditionBuilder();
	}

	public static final class UserAdditionBuilder {
		public String id;
		public String siteId;
		public String projectId;
		public String correlationId;
		public String userId;
		public String  uid;
		public UserAdditionStatus status;

		private UserAdditionBuilder() {
		}

		public UserAdditionBuilder id(String id) {
			this.id = id;
			return this;
		}

		public UserAdditionBuilder projectId(String projectId) {
			this.projectId = projectId;
			return this;
		}

		public UserAdditionBuilder correlationId(String correlationId) {
			this.correlationId = correlationId;
			return this;
		}

		public UserAdditionBuilder userId(String userId) {
			this.userId = userId;
			return this;
		}

		public UserAdditionBuilder uid(String uid) {
			this.uid = uid;
			return this;
		}

		public UserAdditionBuilder siteId(String uid) {
			this.siteId = siteId;
			return this;
		}

		public UserAdditionBuilder status(UserAdditionStatus status) {
			this.status = status;
			return this;
		}

		public UserAddition build() {
			return new UserAddition(id, siteId, projectId, correlationId, userId, uid, status);
		}
	}
}
