/*
 * Copyright (c) 2021 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.domain.sites;

import io.imunity.furms.domain.user_operation.UserAdditionErrorMessage;
import io.imunity.furms.domain.user_operation.UserStatus;

import java.util.Objects;

public class UserProjectsInstallationInfoData {

	private final String projectId;
	private final String name;
	private final String remoteAccountName;
	private final UserStatus status;
	private final UserAdditionErrorMessage errorMessage;

	public UserProjectsInstallationInfoData(String projectId,
	                                        String name,
	                                        String remoteAccountName,
	                                        UserStatus status,
	                                        UserAdditionErrorMessage errorMessage) {
		this.projectId = projectId;
		this.name = name;
		this.remoteAccountName = remoteAccountName;
		this.status = status;
		this.errorMessage = errorMessage;
	}

	public String getProjectId() {
		return projectId;
	}

	public String getName() {
		return name;
	}

	public String getRemoteAccountName() {
		return remoteAccountName;
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
		UserProjectsInstallationInfoData that = (UserProjectsInstallationInfoData) o;
		return Objects.equals(projectId, that.projectId) &&
				Objects.equals(name, that.name) &&
				Objects.equals(remoteAccountName, that.remoteAccountName) &&
				status == that.status &&
				Objects.equals(errorMessage, that.errorMessage);
	}

	@Override
	public int hashCode() {
		return Objects.hash(name, projectId, remoteAccountName, status, errorMessage);
	}

	@Override
	public String toString() {
		return "UserProjectsInstallationInfoData{" +
				"name='" + name + '\'' +
				", projectId='" + projectId + '\'' +
				", remoteAccountName='" + remoteAccountName + '\'' +
				", status=" + status +
				", errorMessage='" + errorMessage + '\'' +
				'}';
	}

	public static UserProjectsInstallationInfoDataBuilder builder() {
		return new UserProjectsInstallationInfoDataBuilder();
	}

	public static final class UserProjectsInstallationInfoDataBuilder {
		private String projectId;
		private String name;
		private String remoteAccountName;
		private UserStatus status;
		private UserAdditionErrorMessage errorMessage;

		private UserProjectsInstallationInfoDataBuilder() {
		}

		public UserProjectsInstallationInfoDataBuilder projectId(String projectId) {
			this.projectId = projectId;
			return this;
		}

		public UserProjectsInstallationInfoDataBuilder name(String name) {
			this.name = name;
			return this;
		}

		public UserProjectsInstallationInfoDataBuilder remoteAccountName(String remoteAccountName) {
			this.remoteAccountName = remoteAccountName;
			return this;
		}

		public UserProjectsInstallationInfoDataBuilder status(UserStatus status) {
			this.status = status;
			return this;
		}

		public UserProjectsInstallationInfoDataBuilder errorMessage(UserAdditionErrorMessage errorMessage) {
			this.errorMessage = errorMessage;
			return this;
		}

		public UserProjectsInstallationInfoData build() {
			return new UserProjectsInstallationInfoData(projectId, name, remoteAccountName, status, errorMessage);
		}
	}
}
