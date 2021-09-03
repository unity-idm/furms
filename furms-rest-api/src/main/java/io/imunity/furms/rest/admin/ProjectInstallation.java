/*
 * Copyright (c) 2020 Bixbit - Krzysztof Benedyczak. All rights reserved.
 * See LICENCE.txt file for licensing information.
 */
package io.imunity.furms.rest.admin;

import java.util.Objects;

class ProjectInstallation {
	public final Project project;
	public final InstallationStatus installationStatus;
	public final String gid;
	
	ProjectInstallation(Project project, InstallationStatus installationStatus, String gid) {
		this.project = project;
		this.installationStatus = installationStatus;
		this.gid = gid;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		ProjectInstallation that = (ProjectInstallation) o;
		return Objects.equals(project, that.project)
				&& installationStatus == that.installationStatus
				&& Objects.equals(gid, that.gid);
	}

	@Override
	public int hashCode() {
		return Objects.hash(project, installationStatus, gid);
	}

	@Override
	public String toString() {
		return "ProjectInstallation{" +
				"project=" + project +
				", installationStatus=" + installationStatus +
				", gid='" + gid + '\'' +
				'}';
	}
}
