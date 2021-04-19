/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.spi.project_allocation_installation;

import io.imunity.furms.domain.project_allocation_installation.ProjectAllocationInstallation;
import io.imunity.furms.domain.project_allocation_installation.ProjectAllocationInstallationStatus;
import io.imunity.furms.domain.site_agent.CorrelationId;

import java.util.Optional;
import java.util.Set;

public interface ProjectAllocationInstallationRepository {
	Set<ProjectAllocationInstallation> findAll(String projectId);

	Optional<ProjectAllocationInstallation> findByCorrelationId(CorrelationId id);

	String create(ProjectAllocationInstallation projectAllocation);

	String update(ProjectAllocationInstallation projectAllocation);

	String update(String id, ProjectAllocationInstallationStatus status);

	boolean exists(String id);

	void delete(String id);

	void deleteAll();
}

