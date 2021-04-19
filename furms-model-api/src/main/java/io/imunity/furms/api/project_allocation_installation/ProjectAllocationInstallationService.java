/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.api.project_allocation_installation;

import io.imunity.furms.domain.project_allocation_installation.ProjectAllocationInstallation;

import java.util.Set;

public interface ProjectAllocationInstallationService {
	Set<ProjectAllocationInstallation> findAll(String communityId, String projectId);

	void create(String communityId, ProjectAllocationInstallation projectAllocationInstallation);

	void delete(String communityId, String id);
}
