/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.domain.resource_access;

import java.util.Objects;

public class UserGrant {
	public final String projectAllocationId;
	public final String userId;
	public final AccessStatus status;
	public final String message;

	UserGrant(String projectAllocationId, String userId, AccessStatus status, String message) {
		this.projectAllocationId = projectAllocationId;
		this.userId = userId;
		this.status = status;
		this.message = message;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		UserGrant userGrant = (UserGrant) o;
		return Objects.equals(projectAllocationId, userGrant.projectAllocationId) && Objects.equals(userId, userGrant.userId) && status == userGrant.status && Objects.equals(message, userGrant.message);
	}

	@Override
	public int hashCode() {
		return Objects.hash(projectAllocationId, userId, status, message);
	}

	@Override
	public String toString() {
		return "UserGrant{" +
			"projectAllocationId='" + projectAllocationId + '\'' +
			", userId='" + userId + '\'' +
			", status=" + status +
			", message='" + message + '\'' +
			'}';
	}

	public static UserGrantBuilder builder() {
		return new UserGrantBuilder();
	}

	public static final class UserGrantBuilder {
		public String projectAllocationId;
		public String userId;
		public AccessStatus status;
		public String message;

		private UserGrantBuilder() {
		}

		public UserGrantBuilder projectAllocationId(String projectAllocationId) {
			this.projectAllocationId = projectAllocationId;
			return this;
		}

		public UserGrantBuilder userId(String userId) {
			this.userId = userId;
			return this;
		}

		public UserGrantBuilder status(AccessStatus status) {
			this.status = status;
			return this;
		}

		public UserGrantBuilder message(String message) {
			this.message = message;
			return this;
		}

		public UserGrant build() {
			return new UserGrant(projectAllocationId, userId, status, message);
		}
	}
}
