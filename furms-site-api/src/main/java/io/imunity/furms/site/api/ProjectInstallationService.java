/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.site.api;

import io.imunity.furms.domain.project_installation.ProjectInstallation;
import io.imunity.furms.domain.project_installation.ProjectInstallationJob;

public interface ProjectInstallationService {
	ProjectInstallation findProjectInstallation(String projectAllocationId);

	void create(ProjectInstallationJob projectInstallationJob);

	void update(ProjectInstallationJob projectInstallationJob);

	void delete(String id);
}
