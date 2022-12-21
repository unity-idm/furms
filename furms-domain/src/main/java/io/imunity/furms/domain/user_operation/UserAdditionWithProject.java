/*
 * Copyright (c) 2021 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.domain.user_operation;

import io.imunity.furms.domain.projects.ProjectId;
import io.imunity.furms.domain.users.FenixUserId;

import java.util.Objects;

public class UserAdditionWithProject {

	private final String siteName;
	private final ProjectId projectId;
	private final String projectName;
	private final String localProjectName;
	private final FenixUserId userId;
	private final UserStatus status;
	private final UserAdditionErrorMessage errorMessage;

	public UserAdditionWithProject(String siteName,
	                               ProjectId projectId,
	                               String projectName,
	                               String localProjectName,
	                               FenixUserId userId,
	                               UserStatus status,
	                               UserAdditionErrorMessage errorMessage) {
		this.siteName = siteName;
		this.projectId = projectId;
		this.projectName = projectName;
		this.localProjectName = localProjectName;
		this.userId = userId;
		this.status = status;
		this.errorMessage = errorMessage;
	}

	public String getSiteName() {
		return siteName;
	}

	public ProjectId getProjectId() {
		return projectId;
	}

	public String getProjectName() {
		return projectName;
	}

	public String getLocalProjectName() {
		return localProjectName;
	}

	public FenixUserId getUserId() {
		return userId;
	}

	public UserStatus getStatus() {
		return status;
	}

	public UserAdditionErrorMessage getErrorMessage() {
		return errorMessage;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		UserAdditionWithProject that = (UserAdditionWithProject) o;
		return Objects.equals(siteName, that.siteName) &&
				Objects.equals(projectId, that.projectId) &&
				Objects.equals(projectName, that.projectName) &&
				Objects.equals(userId, that.userId) &&
				status == that.status &&
				Objects.equals(errorMessage, that.errorMessage);
	}

	@Override
	public int hashCode() {
		return Objects.hash(siteName, projectId, projectName, userId, status, errorMessage);
	}

	@Override
	public String toString() {
		return "UserAdditionWithProject{" +
				"siteName='" + siteName + '\'' +
				", projectId='" + projectId + '\'' +
				", projectName='" + projectName + '\'' +
				", userId='" + userId + '\'' +
				", status=" + status +
				", errorMessage=" + errorMessage +
				'}';
	}

	public static UserAdditionWithProjectBuilder builder() {
		return new UserAdditionWithProjectBuilder();
	}

	public static final class UserAdditionWithProjectBuilder {
		private String siteName;
		private ProjectId projectId;
		private String projectName;
		private String localProjectName;
		private FenixUserId userId;
		private UserStatus status;
		private UserAdditionErrorMessage errorMessage;

		private UserAdditionWithProjectBuilder() {
		}

		public UserAdditionWithProjectBuilder siteName(String siteName) {
			this.siteName = siteName;
			return this;
		}

		public UserAdditionWithProjectBuilder projectName(String projectName) {
			this.projectName = projectName;
			return this;
		}

		public UserAdditionWithProjectBuilder projectId(String projectId) {
			this.projectId = new ProjectId(projectId);
			return this;
		}

		public UserAdditionWithProjectBuilder localProjectName(String localProjectName) {
			this.localProjectName = localProjectName;
			return this;
		}

		public UserAdditionWithProjectBuilder userId(String userId) {
			this.userId = new FenixUserId(userId);
			return this;
		}

		public UserAdditionWithProjectBuilder status(UserStatus status) {
			this.status = status;
			return this;
		}

		public UserAdditionWithProjectBuilder errorMessage(UserAdditionErrorMessage errorMessage) {
			this.errorMessage = errorMessage;
			return this;
		}

		public UserAdditionWithProject build() {
			return new UserAdditionWithProject(siteName, projectId, projectName, localProjectName, userId, status,
				errorMessage);
		}
	}
}
