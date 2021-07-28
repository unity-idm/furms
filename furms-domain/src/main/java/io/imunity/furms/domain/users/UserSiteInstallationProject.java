/*
 * Copyright (c) 2021 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.domain.users;

import java.util.Objects;

public class UserSiteInstallationProject {

	public final String localUserId;
	public final String projectId;

	public UserSiteInstallationProject(String localUserId, String projectId) {
		this.localUserId = localUserId;
		this.projectId = projectId;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		UserSiteInstallationProject that = (UserSiteInstallationProject) o;
		return Objects.equals(localUserId, that.localUserId)
				&& Objects.equals(projectId, that.projectId);
	}

	@Override
	public int hashCode() {
		return Objects.hash(localUserId, projectId);
	}

	@Override
	public String toString() {
		return "UserSiteInstallationProject{" +
				"localUserId='" + localUserId + '\'' +
				", projectId='" + projectId + '\'' +
				'}';
	}

	public static UserSiteInstallationProjectBuilder builder() {
		return new UserSiteInstallationProjectBuilder();
	}

	public static final class UserSiteInstallationProjectBuilder {
		private String localUserId;
		private String projectId;

		private UserSiteInstallationProjectBuilder() {
		}

		public UserSiteInstallationProjectBuilder localUserId(String localUserId) {
			this.localUserId = localUserId;
			return this;
		}

		public UserSiteInstallationProjectBuilder projectId(String projectId) {
			this.projectId = projectId;
			return this;
		}

		public UserSiteInstallationProject build() {
			return new UserSiteInstallationProject(localUserId, projectId);
		}
	}
}
