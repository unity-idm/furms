/*
 * Copyright (c) 2021 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.domain.sites;

import io.imunity.furms.domain.projects.ProjectId;

import java.util.Objects;

public class SiteInstalledProject {
	public final SiteId siteId;
	public final String siteName;
	public final ProjectId projectId;
	public final Gid gid;

	public SiteInstalledProject(SiteId siteId, String siteName, ProjectId projectId, Gid gid) {
		this.siteId = siteId;
		this.siteName = siteName;
		this.projectId = projectId;
		this.gid = gid;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		SiteInstalledProject that = (SiteInstalledProject) o;
		return Objects.equals(siteId, that.siteId)
				&& Objects.equals(siteName, that.siteName)
				&& Objects.equals(projectId, that.projectId)
				&& Objects.equals(gid, that.gid);
	}

	@Override
	public int hashCode() {
		return Objects.hash(siteId, siteName, projectId, gid);
	}

	@Override
	public String toString() {
		return "SiteInstalledProject{" +
				"siteId='" + siteId + '\'' +
				", siteName='" + siteName + '\'' +
				", projectId='" + projectId + '\'' +
				", gid=" + gid +
				'}';
	}

	public static SiteInstalledProjectBuilder builder() {
		return new SiteInstalledProjectBuilder();
	}

	public static final class SiteInstalledProjectBuilder {
		public SiteId siteId;
		public String siteName;
		public ProjectId projectId;
		public Gid gid;

		private SiteInstalledProjectBuilder() {
		}

		public SiteInstalledProjectBuilder siteId(SiteId siteId) {
			this.siteId = siteId;
			return this;
		}

		public SiteInstalledProjectBuilder siteName(String siteName) {
			this.siteName = siteName;
			return this;
		}

		public SiteInstalledProjectBuilder projectId(ProjectId projectId) {
			this.projectId = projectId;
			return this;
		}

		public SiteInstalledProjectBuilder gid(Gid gid) {
			this.gid = gid;
			return this;
		}

		public SiteInstalledProject build() {
			return new SiteInstalledProject(siteId, siteName, projectId, gid);
		}
	}
}
