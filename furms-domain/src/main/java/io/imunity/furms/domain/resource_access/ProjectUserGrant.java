/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.domain.resource_access;

import java.util.Objects;

public class ProjectUserGrant {
	public final String projectId;
	public final String userId;

	public ProjectUserGrant(String projectId, String userId) {
		this.projectId = projectId;
		this.userId = userId;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		ProjectUserGrant userGrant = (ProjectUserGrant) o;
		return Objects.equals(projectId, userGrant.projectId) &&
			Objects.equals(userId, userGrant.userId);
	}

	@Override
	public int hashCode() {
		return Objects.hash(projectId, userId);
	}

	@Override
	public String toString() {
		return "UserGrant{" +
			"projectId='" + projectId + '\'' +
			", userId='" + userId + '\'' +
			'}';
	}

	public static UserGrantBuilder builder() {
		return new UserGrantBuilder();
	}

	public static final class UserGrantBuilder {
		public String projectId;
		public String userId;

		private UserGrantBuilder() {
		}

		public UserGrantBuilder projectId(String projectId) {
			this.projectId = projectId;
			return this;
		}

		public UserGrantBuilder userId(String userId) {
			this.userId = userId;
			return this;
		}

		public ProjectUserGrant build() {
			return new ProjectUserGrant(projectId, userId);
		}
	}
}
