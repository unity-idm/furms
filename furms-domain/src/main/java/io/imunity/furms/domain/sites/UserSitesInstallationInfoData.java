/*
 * Copyright (c) 2021 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.domain.sites;

import java.util.Objects;
import java.util.Set;

public class UserSitesInstallationInfoData {

	private final String siteName;
	private final String connectionInfo;
	private final Set<UserProjectsInstallationInfoData> projects;

	public UserSitesInstallationInfoData(String siteName,
	                                     String connectionInfo,
	                                     Set<UserProjectsInstallationInfoData> projects) {
		this.siteName = siteName;
		this.connectionInfo = connectionInfo;
		this.projects = projects;
	}

	public String getSiteName() {
		return siteName;
	}

	public String getConnectionInfo() {
		return connectionInfo;
	}

	public Set<UserProjectsInstallationInfoData> getProjects() {
		return projects;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		UserSitesInstallationInfoData that = (UserSitesInstallationInfoData) o;
		return Objects.equals(siteName, that.siteName) &&
				Objects.equals(connectionInfo, that.connectionInfo) &&
				Objects.equals(projects, that.projects);
	}

	@Override
	public int hashCode() {
		return Objects.hash(siteName, connectionInfo, projects);
	}

	@Override
	public String toString() {
		return "UserSitesInstallationInfoData{" +
				"siteName='" + siteName + '\'' +
				", projects=" + projects +
				'}';
	}

	public static UserSitesInstallationInfoDataBuilder builder() {
		return new UserSitesInstallationInfoDataBuilder();
	}

	public static final class UserSitesInstallationInfoDataBuilder {
		private String siteName;
		private String connectionInfo;
		private Set<UserProjectsInstallationInfoData> projects;

		private UserSitesInstallationInfoDataBuilder() {
		}

		public UserSitesInstallationInfoDataBuilder siteName(String siteName) {
			this.siteName = siteName;
			return this;
		}

		public UserSitesInstallationInfoDataBuilder connectionInfo(String connectionInfo) {
			this.connectionInfo = connectionInfo;
			return this;
		}

		public UserSitesInstallationInfoDataBuilder projects(Set<UserProjectsInstallationInfoData> projects) {
			this.projects = projects;
			return this;
		}

		public UserSitesInstallationInfoData build() {
			return new UserSitesInstallationInfoData(siteName, connectionInfo, projects);
		}
	}
}
