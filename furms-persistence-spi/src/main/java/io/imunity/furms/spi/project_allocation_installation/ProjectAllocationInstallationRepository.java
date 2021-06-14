/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.spi.project_allocation_installation;

import io.imunity.furms.domain.project_allocation_installation.*;
import io.imunity.furms.domain.site_agent.CorrelationId;

import java.util.Optional;
import java.util.Set;

public interface ProjectAllocationInstallationRepository {
	Set<ProjectAllocationInstallation> findAll(String projectId);

	Set<ProjectAllocationInstallation> findAll(String projectId, String siteId);

	ProjectAllocationInstallation findByProjectAllocationId(String projectAllocationId);

	Set<ProjectDeallocation> findAllDeallocation(String projectId);

	Set<ProjectAllocationChunk> findAllChunks(String projectId);

	Optional<ProjectAllocationInstallation> findByCorrelationId(CorrelationId id);

	String create(ProjectAllocationInstallation projectAllocation);

	String create(ProjectDeallocation projectDeallocation);

	String create(ProjectAllocationChunk projectAllocationChunk);

	String update(String correlationId, ProjectAllocationInstallationStatus status, Optional<ErrorMessage> errorMessage);

	ProjectDeallocation findDeallocationByCorrelationId(String correlationId);

	String update(String correlationId, ProjectDeallocationStatus status, Optional<ErrorMessage> errorMessage);

	boolean exists(String id);

	boolean chunksExist(String projectAllocationId);

	void deleteBy(String id);

	void deleteAll();
}

