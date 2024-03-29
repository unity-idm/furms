/*
 * Copyright (c) 2021 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.domain.resource_access;

import com.google.common.collect.ImmutableList;
import io.imunity.furms.domain.projects.ProjectId;
import io.imunity.furms.domain.users.FenixUserId;

import java.util.List;
import java.util.Objects;

public class UsersWithProjectAccess {

	private final ProjectId projectId;
	private final List<FenixUserId> userIds;

	public UsersWithProjectAccess(ProjectId projectId, List<FenixUserId> userIds) {
		this.projectId = projectId;
		this.userIds = ImmutableList.copyOf(userIds);
	}

	public ProjectId getProjectId() {
		return projectId;
	}

	public List<FenixUserId> getUserIds() {
		return userIds;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		UsersWithProjectAccess that = (UsersWithProjectAccess) o;
		return Objects.equals(projectId, that.projectId) && Objects.equals(userIds, that.userIds);
	}

	@Override
	public int hashCode() {
		return Objects.hash(projectId, userIds);
	}

	@Override
	public String toString() {
		return "ProjectUsers{" +
				"projectId='" + projectId + '\'' +
				", userIds=" + userIds +
				'}';
	}

	public static ProjectUsersBuilder builder() {
		return new ProjectUsersBuilder();
	}

	public static final class ProjectUsersBuilder {
		private ProjectId projectId;
		private List<FenixUserId> userIds;

		private ProjectUsersBuilder() {
		}

		public ProjectUsersBuilder projectId(ProjectId projectId) {
			this.projectId = projectId;
			return this;
		}

		public ProjectUsersBuilder userIds(List<FenixUserId> userIds) {
			this.userIds = userIds;
			return this;
		}

		public UsersWithProjectAccess build() {
			return new UsersWithProjectAccess(projectId, userIds);
		}
	}
}
