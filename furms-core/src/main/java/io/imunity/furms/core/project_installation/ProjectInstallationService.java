/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.core.project_installation;

import io.imunity.furms.domain.project_installation.ProjectInstallation;

public interface ProjectInstallationService {
	ProjectInstallation findProjectInstallation(String communityId, String projectAllocationId);
	boolean existsByProjectId(String communityId, String projectId);
	void create(String communityId, String projectId);
	void update(String communityId, String projectId);
	void remove(String communityId, String projectId);
}
