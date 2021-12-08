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

	Optional<ProjectDeallocation> findDeallocationByProjectAllocationId(String projectAllocationId);

	Set<ProjectDeallocation> findAllDeallocation(String projectId);

	Set<ProjectAllocationChunk> findAllChunks(String projectId);

	Optional<ProjectAllocationInstallation> findByCorrelationId(CorrelationId id);

	String create(ProjectAllocationInstallation projectAllocation);

	String create(ProjectDeallocation projectDeallocation);

	void update(String projectAllocationId, ProjectAllocationInstallationStatus status, CorrelationId correlationId);

	String create(ProjectAllocationChunk projectAllocationChunk);

	void update(ProjectAllocationChunk projectAllocationChunk);

	void update(String correlationId, ProjectAllocationInstallationStatus status, Optional<ErrorMessage> errorMessage);

	ProjectDeallocation findDeallocationByCorrelationId(String correlationId);

	void update(String correlationId, ProjectDeallocationStatus status, Optional<ErrorMessage> errorMessage);

	boolean exists(String id);

	void deleteBy(String id);

	void deleteAll();

	void delete(CorrelationId id);
}

