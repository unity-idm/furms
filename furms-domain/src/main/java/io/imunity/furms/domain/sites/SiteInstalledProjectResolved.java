/*
 * Copyright (c) 2021 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.domain.sites;

import io.imunity.furms.domain.projects.Project;

import java.util.Objects;

public class SiteInstalledProjectResolved {
	public final String siteId;
	public final String siteName;
	public final Project project;
	public final Gid gid;

	public SiteInstalledProjectResolved(String siteId, String siteName, Project project, Gid gid) {
		this.siteId = siteId;
		this.siteName = siteName;
		this.project = project;
		this.gid = gid;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		SiteInstalledProjectResolved that = (SiteInstalledProjectResolved) o;
		return Objects.equals(siteId, that.siteId)
				&& Objects.equals(siteName, that.siteName)
				&& Objects.equals(project, that.project)
				&& Objects.equals(gid, that.gid);
	}

	@Override
	public int hashCode() {
		return Objects.hash(siteId, siteName, project, gid);
	}

	@Override
	public String toString() {
		return "SiteInstalledProjectResolved{" +
				"siteId='" + siteId + '\'' +
				", siteName='" + siteName + '\'' +
				", project=" + project +
				", gid=" + gid +
				'}';
	}

	public SiteInstalledProject toSiteInstalledProject() {
		return SiteInstalledProject.builder()
				.siteId(siteId)
				.siteName(siteName)
				.projectId(project.getId())
				.gid(gid)
				.build();
	}

	public static SiteInstalledProjectBuilder builder() {
		return new SiteInstalledProjectBuilder();
	}

	public static final class SiteInstalledProjectBuilder {
		public String siteId;
		public String siteName;
		public Project project;
		public Gid gid;

		private SiteInstalledProjectBuilder() {
		}

		public SiteInstalledProjectBuilder siteId(String siteId) {
			this.siteId = siteId;
			return this;
		}

		public SiteInstalledProjectBuilder siteName(String siteName) {
			this.siteName = siteName;
			return this;
		}

		public SiteInstalledProjectBuilder project(Project project) {
			this.project = project;
			return this;
		}

		public SiteInstalledProjectBuilder gid(Gid gid) {
			this.gid = gid;
			return this;
		}

		public SiteInstalledProjectResolved build() {
			return new SiteInstalledProjectResolved(siteId, siteName, project, gid);
		}
	}
}
