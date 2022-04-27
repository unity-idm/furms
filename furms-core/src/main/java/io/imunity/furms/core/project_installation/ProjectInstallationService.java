/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.core.project_installation;

import io.imunity.furms.domain.project_allocation.ProjectAllocationId;
import io.imunity.furms.domain.project_installation.ProjectInstallation;
import io.imunity.furms.domain.projects.Project;
import io.imunity.furms.domain.projects.ProjectId;
import io.imunity.furms.domain.sites.SiteId;

public interface ProjectInstallationService {
	ProjectInstallation findProjectInstallationOfProjectAllocation(ProjectAllocationId projectAllocationId);
	boolean isProjectInstalled(SiteId siteId, ProjectId projectId);
	boolean isProjectInTerminalState(ProjectId projectId);
	void createOrUpdate(ProjectId projectId, ProjectInstallation projectInstallation);
	void update(Project project);
	void remove(ProjectId projectId);
}
