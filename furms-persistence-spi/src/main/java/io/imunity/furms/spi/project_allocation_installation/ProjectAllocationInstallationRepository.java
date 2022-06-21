/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.spi.project_allocation_installation;

import io.imunity.furms.domain.project_allocation.ProjectAllocationId;
import io.imunity.furms.domain.project_allocation_installation.*;
import io.imunity.furms.domain.projects.ProjectId;
import io.imunity.furms.domain.site_agent.CorrelationId;
import io.imunity.furms.domain.sites.SiteId;

import java.util.Optional;
import java.util.Set;

public interface ProjectAllocationInstallationRepository {
	Set<ProjectAllocationInstallation> findAll(ProjectId projectId);

	Set<ProjectAllocationInstallation> findAll(ProjectId projectId, SiteId siteId);

	ProjectAllocationInstallation findByProjectAllocationId(ProjectAllocationId projectAllocationId);

	Optional<ProjectDeallocation> findDeallocationByProjectAllocationId(ProjectAllocationId projectAllocationId);

	Set<ProjectDeallocation> findAllDeallocation(ProjectId projectId);

	Set<ProjectAllocationChunk> findAllChunks(ProjectId projectId);

	Set<ProjectAllocationChunk> findAllChunksByAllocationId(ProjectAllocationId projectAllocationId);

	Optional<ProjectAllocationInstallation> findByCorrelationId(CorrelationId id);

	ProjectAllocationInstallationId create(ProjectAllocationInstallation projectAllocation);

	ProjectDeallocationId create(ProjectDeallocation projectDeallocation);

	void update(ProjectAllocationId projectAllocationId, ProjectAllocationInstallationStatus status, CorrelationId correlationId);

	void create(ProjectAllocationChunk projectAllocationChunk);

	void updateByProjectAllocationId(ProjectAllocationId projectAllocationId, ProjectAllocationInstallationStatus status,
	                                 Optional<ErrorMessage> errorMessage);

	void update(ProjectAllocationChunk projectAllocationChunk);

	void update(CorrelationId correlationId, ProjectAllocationInstallationStatus status, Optional<ErrorMessage> errorMessage);

	Optional<ProjectDeallocation> findDeallocationByCorrelationId(CorrelationId correlationId);

	void update(CorrelationId correlationId, ProjectDeallocationStatus status, Optional<ErrorMessage> errorMessage);

	boolean exists(ProjectAllocationInstallationId id);

	void deleteBy(ProjectAllocationInstallationId id);

	void deleteAll();

	void delete(CorrelationId id);
}

