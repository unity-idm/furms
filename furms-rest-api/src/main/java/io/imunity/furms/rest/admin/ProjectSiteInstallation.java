/*
 * Copyright (c) 2021 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */
package io.imunity.furms.rest.admin;

import io.imunity.furms.domain.sites.SiteInstalledProject;

import java.util.Objects;

class ProjectSiteInstallation {
	public final String siteId;
	public final String gid;

	ProjectSiteInstallation(String siteId, String gid) {
		this.siteId = siteId;
		this.gid = gid;
	}

	ProjectSiteInstallation(SiteInstalledProject installation) {
		this(installation.siteId, installation.gid.id);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		ProjectSiteInstallation that = (ProjectSiteInstallation) o;
		return Objects.equals(siteId, that.siteId)
				&& Objects.equals(gid, that.gid);
	}

	@Override
	public int hashCode() {
		return Objects.hash(siteId, gid);
	}

	@Override
	public String toString() {
		return "ProjectSiteInstallation{" +
				"siteId='" + siteId + '\'' +
				", gid='" + gid + '\'' +
				'}';
	}
}
