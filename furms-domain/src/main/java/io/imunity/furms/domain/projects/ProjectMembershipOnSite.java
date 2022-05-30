/*
 * Copyright (c) 2021 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.domain.projects;

import java.util.Objects;

public class ProjectMembershipOnSite {

	public final String localUserId;
	public final ProjectId projectId;

	public ProjectMembershipOnSite(String localUserId, ProjectId projectId) {
		this.localUserId = localUserId;
		this.projectId = projectId;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		ProjectMembershipOnSite that = (ProjectMembershipOnSite) o;
		return Objects.equals(localUserId, that.localUserId)
				&& Objects.equals(projectId, that.projectId);
	}

	@Override
	public int hashCode() {
		return Objects.hash(localUserId, projectId);
	}

	@Override
	public String toString() {
		return "ProjectMembershipOnSite{" +
				"localUserId='" + localUserId + '\'' +
				", projectId='" + projectId + '\'' +
				'}';
	}

	public static ProjectMembershipOnSiteBuilder builder() {
		return new ProjectMembershipOnSiteBuilder();
	}

	public static final class ProjectMembershipOnSiteBuilder {
		private String localUserId;
		private ProjectId projectId;

		private ProjectMembershipOnSiteBuilder() {
		}

		public ProjectMembershipOnSiteBuilder localUserId(String localUserId) {
			this.localUserId = localUserId;
			return this;
		}

		public ProjectMembershipOnSiteBuilder projectId(ProjectId projectId) {
			this.projectId = projectId;
			return this;
		}

		public ProjectMembershipOnSite build() {
			return new ProjectMembershipOnSite(localUserId, projectId);
		}
	}
}
