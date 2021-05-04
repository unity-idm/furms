/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.core.project_allocation_installation;

import io.imunity.furms.domain.project_allocation.ProjectAllocationResolved;
import io.imunity.furms.domain.project_allocation_installation.ProjectAllocationInstallation;

import java.util.Set;

public interface ProjectAllocationInstallationService {
	Set<ProjectAllocationInstallation> findAll(String communityId, String projectId);
	Set<ProjectAllocationInstallation> findAll(String projectId);
	void createAllocation(String communityId, ProjectAllocationInstallation projectAllocationInstallation, ProjectAllocationResolved projectAllocationResolved);
	void createDeallocation(String communityId, ProjectAllocationResolved projectAllocationResolved);
}
