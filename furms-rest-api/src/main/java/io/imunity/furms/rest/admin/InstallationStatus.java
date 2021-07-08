/*
 * Copyright (c) 2020 Bixbit - Krzysztof Benedyczak. All rights reserved.
 * See LICENCE.txt file for licensing information.
 */
package io.imunity.furms.rest.admin;

import io.imunity.furms.domain.project_installation.ProjectInstallationStatus;

enum InstallationStatus {
	PENDING,
	ACKNOWLEDGED,
	INSTALLED,
	FAILED;

	public static InstallationStatus valueOf(ProjectInstallationStatus status) {
		return InstallationStatus.valueOf(status.name());
	}
}
