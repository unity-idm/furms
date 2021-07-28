/*
 * Copyright (c) 2021 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.rest.cidp;

import io.imunity.furms.domain.users.UserSiteInstallationProject;

import java.util.Objects;

public class UserSiteInstallationProjectJson {

	public final String localUserId;
	public final String projectId;

	public UserSiteInstallationProjectJson(UserSiteInstallationProject project) {
		this.localUserId = project.localUserId;
		this.projectId = project.projectId;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		UserSiteInstallationProjectJson that = (UserSiteInstallationProjectJson) o;
		return Objects.equals(localUserId, that.localUserId)
				&& Objects.equals(projectId, that.projectId);
	}

	@Override
	public int hashCode() {
		return Objects.hash(localUserId, projectId);
	}

	@Override
	public String toString() {
		return "UserSiteInstallationProjectJson{" +
				"localUserId='" + localUserId + '\'' +
				", projectId='" + projectId + '\'' +
				'}';
	}

}
