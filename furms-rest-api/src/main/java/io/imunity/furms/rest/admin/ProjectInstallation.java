/*
 * Copyright (c) 2020 Bixbit - Krzysztof Benedyczak. All rights reserved.
 * See LICENCE.txt file for licensing information.
 */
package io.imunity.furms.rest.admin;

import java.time.ZonedDateTime;
import java.util.Objects;

class ProjectInstallation {
	public final Project project;
	public final InstallationStatus installationStatus;
	public final ZonedDateTime lastStatusChangeOn;
	public final String gid;
	
	ProjectInstallation(Project project, InstallationStatus installationStatus,
			ZonedDateTime lastStatusChangeOn, String gid) {
		this.project = project;
		this.installationStatus = installationStatus;
		this.lastStatusChangeOn = lastStatusChangeOn;
		this.gid = gid;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		ProjectInstallation that = (ProjectInstallation) o;
		return Objects.equals(project, that.project)
				&& installationStatus == that.installationStatus
				&& Objects.equals(lastStatusChangeOn, that.lastStatusChangeOn)
				&& Objects.equals(gid, that.gid);
	}

	@Override
	public int hashCode() {
		return Objects.hash(project, installationStatus, lastStatusChangeOn, gid);
	}

	@Override
	public String toString() {
		return "ProjectInstallation{" +
				"project=" + project +
				", installationStatus=" + installationStatus +
				", lastStatusChangeOn=" + lastStatusChangeOn +
				", gid='" + gid + '\'' +
				'}';
	}
}
