/*
 * Copyright (c) 2021 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.domain.user_operation;

import java.util.Objects;

public class UserAdditionWithProject {

	private final String siteName;
	private final String projectName;
	private final String userId;
	private final UserStatus status;
	private final UserAdditionErrorMessage errorMessage;

	public UserAdditionWithProject(String siteName,
	                               String projectName,
	                               String userId,
	                               UserStatus status,
	                               UserAdditionErrorMessage errorMessage) {
		this.siteName = siteName;
		this.projectName = projectName;
		this.userId = userId;
		this.status = status;
		this.errorMessage = errorMessage;
	}

	public String getSiteName() {
		return siteName;
	}

	public String getProjectName() {
		return projectName;
	}

	public String getUserId() {
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
				Objects.equals(projectName, that.projectName) &&
				Objects.equals(userId, that.userId) &&
				status == that.status &&
				Objects.equals(errorMessage, that.errorMessage);
	}

	@Override
	public int hashCode() {
		return Objects.hash(siteName, projectName, userId, status, errorMessage);
	}

	@Override
	public String toString() {
		return "UserAdditionWithProject{" +
				"siteName='" + siteName + '\'' +
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
		private String projectName;
		private String userId;
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

		public UserAdditionWithProjectBuilder userId(String userId) {
			this.userId = userId;
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
			return new UserAdditionWithProject(siteName, projectName, userId, status, errorMessage);
		}
	}
}
