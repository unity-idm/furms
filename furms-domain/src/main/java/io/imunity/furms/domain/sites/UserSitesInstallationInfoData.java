/*
 * Copyright (c) 2021 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.domain.sites;

import java.util.Objects;
import java.util.Set;

public class UserSitesInstallationInfoData {

	private final SiteId siteId;
	private final String siteName;
	private final String oauthClientId;
	private final String connectionInfo;
	private final Set<UserProjectsInstallationInfoData> projects;

	public UserSitesInstallationInfoData(SiteId siteId,
	                                     String siteName,
	                                     String oauthClientId,
	                                     String connectionInfo,
	                                     Set<UserProjectsInstallationInfoData> projects) {
		this.siteId = siteId;
		this.siteName = siteName;
		this.oauthClientId = oauthClientId;
		this.connectionInfo = connectionInfo;
		this.projects = projects;
	}

	public SiteId getSiteId() {
		return siteId;
	}

	public String getSiteName() {
		return siteName;
	}

	public String getOauthClientId() {
		return oauthClientId;
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
		return Objects.equals(siteId, that.siteId)
				&& Objects.equals(siteName, that.siteName)
				&& Objects.equals(oauthClientId, that.oauthClientId)
				&& Objects.equals(connectionInfo, that.connectionInfo)
				&& Objects.equals(projects, that.projects);
	}

	@Override
	public int hashCode() {
		return Objects.hash(siteId, siteName, oauthClientId, connectionInfo, projects);
	}

	@Override
	public String toString() {
		return "UserSitesInstallationInfoData{" +
				"siteId='" + siteId + '\'' +
				", siteName='" + siteName + '\'' +
				", oauthClientId='" + oauthClientId + '\'' +
				", connectionInfo='" + connectionInfo + '\'' +
				", projects=" + projects +
				'}';
	}

	public static UserSitesInstallationInfoDataBuilder builder() {
		return new UserSitesInstallationInfoDataBuilder();
	}

	public static final class UserSitesInstallationInfoDataBuilder {
		private SiteId siteId;
		private String siteName;
		private String oauthClientId;
		private String connectionInfo;
		private Set<UserProjectsInstallationInfoData> projects;

		private UserSitesInstallationInfoDataBuilder() {
		}

		public UserSitesInstallationInfoDataBuilder siteId(SiteId siteId) {
			this.siteId = siteId;
			return this;
		}

		public UserSitesInstallationInfoDataBuilder siteName(String siteName) {
			this.siteName = siteName;
			return this;
		}

		public UserSitesInstallationInfoDataBuilder oauthClientId(String oauthClientId) {
			this.oauthClientId = oauthClientId;
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
			return new UserSitesInstallationInfoData(siteId, siteName, oauthClientId, connectionInfo, projects);
		}
	}
}
