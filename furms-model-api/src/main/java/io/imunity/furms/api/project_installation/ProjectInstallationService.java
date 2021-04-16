/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.api.project_installation;

import io.imunity.furms.domain.project_installation.ProjectInstallation;
import io.imunity.furms.domain.project_installation.ProjectInstallationJob;

public interface ProjectInstallationService {
	ProjectInstallation findProjectInstallation(String communityId, String projectAllocationId);

	void create(String communityId, ProjectInstallationJob projectInstallationJob);

	void delete(String communityId, String id);
}
