/*
 * Copyright (c) 2021 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.views.user_settings.sites;

import io.imunity.furms.domain.user_operation.UserStatus;

import java.util.Objects;

public class UserSitesGridModel {

	private final String siteName;
	private final String connectionInfo;
	private final String projectName;
	private final String remoteAccountName;
	private final UserStatus status;
	private final String errorMessage;

	public UserSitesGridModel(String siteName,
	                          String connectionInfo,
	                          String projectName,
	                          String remoteAccountName,
	                          UserStatus status,
	                          String errorMessage) {
		this.siteName = siteName;
		this.connectionInfo = connectionInfo;
		this.projectName = projectName;
		this.remoteAccountName = remoteAccountName;
		this.status = status;
		this.errorMessage = errorMessage;
	}

	public String getSiteName() {
		return siteName;
	}

	public String getConnectionInfo() {
		return connectionInfo;
	}

	public String getProjectName() {
		return projectName;
	}

	public String getRemoteAccountName() {
		return remoteAccountName;
	}

	public UserStatus getStatus() {
		return status;
	}

	public String getErrorMessage() {
		return errorMessage;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		UserSitesGridModel that = (UserSitesGridModel) o;
		return Objects.equals(siteName, that.siteName) &&
				Objects.equals(connectionInfo, that.connectionInfo) &&
				Objects.equals(projectName, that.projectName) &&
				Objects.equals(remoteAccountName, that.remoteAccountName) &&
				status == that.status &&
				Objects.equals(errorMessage, that.errorMessage);
	}

	@Override
	public int hashCode() {
		return Objects.hash(siteName, connectionInfo, projectName, remoteAccountName, status, errorMessage);
	}

	@Override
	public String toString() {
		return "UserSitesGridModel{" +
				"siteName='" + siteName + '\'' +
				", connectionInfo='" + connectionInfo + '\'' +
				", projectName='" + projectName + '\'' +
				", remoteAccountName='" + remoteAccountName + '\'' +
				", status=" + status +
				", errorMessage='" + errorMessage + '\'' +
				'}';
	}

	public static UserSitesGridModelBuilder builder() {
		return new UserSitesGridModelBuilder();
	}


	public static final class UserSitesGridModelBuilder {
		private String siteName;
		private String connectionInfo;
		private String projectName;
		private String remoteAccountName;
		private UserStatus status;
		private String errorMessage;

		private UserSitesGridModelBuilder() {
		}

		public UserSitesGridModelBuilder siteName(String siteName) {
			this.siteName = siteName;
			return this;
		}

		public UserSitesGridModelBuilder connectionInfo(String connectionInfo) {
			this.connectionInfo = connectionInfo;
			return this;
		}

		public UserSitesGridModelBuilder projectName(String projectName) {
			this.projectName = projectName;
			return this;
		}

		public UserSitesGridModelBuilder remoteAccountName(String remoteAccountName) {
			this.remoteAccountName = remoteAccountName;
			return this;
		}

		public UserSitesGridModelBuilder status(UserStatus status) {
			this.status = status;
			return this;
		}

		public UserSitesGridModelBuilder errorMessage(String errorMessage) {
			this.errorMessage = errorMessage;
			return this;
		}

		public UserSitesGridModel build() {
			return new UserSitesGridModel(siteName, connectionInfo, projectName, remoteAccountName, status, errorMessage);
		}
	}
}
