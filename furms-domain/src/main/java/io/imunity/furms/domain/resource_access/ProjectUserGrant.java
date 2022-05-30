/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.domain.resource_access;

import io.imunity.furms.domain.projects.ProjectId;
import io.imunity.furms.domain.sites.SiteId;
import io.imunity.furms.domain.users.FenixUserId;

import java.util.Objects;

public class ProjectUserGrant {
	public final SiteId siteId;
	public final GrantId grantId;
	public final ProjectId projectId;
	public final FenixUserId userId;

	public ProjectUserGrant(SiteId siteId, GrantId grantId, ProjectId projectId, FenixUserId userId) {
		this.siteId = siteId;
		this.grantId = grantId;
		this.projectId = projectId;
		this.userId = userId;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		ProjectUserGrant that = (ProjectUserGrant) o;
		return Objects.equals(grantId, that.grantId) &&
			Objects.equals(siteId, that.siteId) &&
			Objects.equals(projectId, that.projectId) &&
			Objects.equals(userId, that.userId);
	}

	@Override
	public int hashCode() {
		return Objects.hash(siteId, grantId, projectId, userId);
	}

	@Override
	public String toString() {
		return "ProjectUserGrant{" +
			"grantId='" + grantId + '\'' +
			", siteId='" + siteId + '\'' +
			", projectId='" + projectId + '\'' +
			", userId=" + userId +
			'}';
	}
}
