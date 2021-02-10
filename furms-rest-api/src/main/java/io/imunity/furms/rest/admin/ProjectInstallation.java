/*
 * Copyright (c) 2020 Bixbit - Krzysztof Benedyczak. All rights reserved.
 * See LICENCE.txt file for licensing information.
 */
package io.imunity.furms.rest.admin;

import java.time.ZonedDateTime;

class ProjectInstallation {
	
	final Project project;
	final InstallationStatus installationStatus;
	final ZonedDateTime lastStatusChangeOn;
	final String gid;
	
	ProjectInstallation(Project project, InstallationStatus installationStatus,
			ZonedDateTime lastStatusChangeOn, String gid) {
		this.project = project;
		this.installationStatus = installationStatus;
		this.lastStatusChangeOn = lastStatusChangeOn;
		this.gid = gid;
	}
}
