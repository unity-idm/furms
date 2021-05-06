/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.core.project_installation;

import io.imunity.furms.domain.project_installation.ProjectInstallation;
import io.imunity.furms.domain.projects.Project;

public interface ProjectInstallationService {
	ProjectInstallation findProjectInstallation(String projectAllocationId);
	boolean existsByProjectId(String siteId, String projectId);
	void create(String projectId, ProjectInstallation projectInstallation);
	void update(Project project);
	void remove(String projectId);
}
