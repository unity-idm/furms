/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.api.project_allocation;

import io.imunity.furms.domain.project_allocation.ProjectAllocation;
import io.imunity.furms.domain.project_allocation.ProjectAllocationResolved;
import io.imunity.furms.domain.project_allocation_installation.ProjectAllocationChunk;
import io.imunity.furms.domain.project_allocation_installation.ProjectAllocationChunkResolved;
import io.imunity.furms.domain.project_allocation_installation.ProjectAllocationInstallation;
import io.imunity.furms.domain.project_allocation_installation.ProjectDeallocation;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Optional;
import java.util.Set;

public interface ProjectAllocationService {
	Optional<ProjectAllocation> findByProjectIdAndId(String projectId, String id);

	Optional<ProjectAllocationResolved> findWithRelatedObjectsByProjectIdAndId(String projectId, String id);

	Optional<ProjectAllocationResolved> findByIdWithRelatedObjects(String communityId, String id);

	BigDecimal getAvailableAmount(String communityId, String communityAllocationId);

	Set<ProjectAllocation> findAll(String communityId, String projectId);

	Set<ProjectAllocationResolved> findAllWithRelatedObjects(String communityId, String projectId);

	Set<ProjectDeallocation> findAllUninstallations(String projectId);

	Set<ProjectAllocationChunk> findAllChunks(String projectId);

	Set<ProjectAllocationChunkResolved> findAllChunksBySiteId(String siteId);

	Set<ProjectAllocationChunkResolved> findAllChunksBySiteIdAndProjectId(String siteId, String projectId);

	Set<ProjectAllocationInstallation> findAllInstallations(String projectId);

	Set<ProjectAllocationResolved> findAllWithRelatedObjects(String projectId);

	Set<ProjectAllocationResolved> findAllWithRelatedObjectsBySiteId(String siteId);

	Set<ProjectAllocationResolved> findAllWithRelatedObjectsBySiteIdAndProjectId(String siteId, String projectId);

	void create(String communityId, ProjectAllocation projectAllocation);

	void update(String communityId, ProjectAllocation projectAllocation);

	void delete(String communityId, String id);
}
