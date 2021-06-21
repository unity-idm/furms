/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.core.project_installation;

import io.imunity.furms.domain.project_installation.ProjectInstallation;
import io.imunity.furms.domain.projects.Project;

public interface ProjectInstallationService {
	ProjectInstallation findProjectInstallationOfProjectAllocation(String projectAllocationId);
	boolean isProjectInstalled(String siteId, String projectId);
	boolean isProjectInTerminalState(String projectId);
	void create(String projectId, ProjectInstallation projectInstallation);
	void update(Project project);
	void remove(String projectId);
}
