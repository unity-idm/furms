/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.api.project_allocation;

import io.imunity.furms.domain.communities.CommunityId;
import io.imunity.furms.domain.community_allocation.CommunityAllocationId;
import io.imunity.furms.domain.project_allocation.ProjectAllocation;
import io.imunity.furms.domain.project_allocation.ProjectAllocationId;
import io.imunity.furms.domain.project_allocation.ProjectAllocationResolved;
import io.imunity.furms.domain.project_allocation_installation.ProjectAllocationChunk;
import io.imunity.furms.domain.project_allocation_installation.ProjectAllocationChunkResolved;
import io.imunity.furms.domain.project_allocation_installation.ProjectAllocationInstallation;
import io.imunity.furms.domain.project_allocation_installation.ProjectDeallocation;
import io.imunity.furms.domain.projects.ProjectId;
import io.imunity.furms.domain.sites.SiteId;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.Set;

public interface ProjectAllocationService {
	Optional<ProjectAllocation> findByProjectIdAndId(ProjectId projectId, ProjectAllocationId id);

	Optional<ProjectAllocationResolved> findByIdValidatingProjectsWithRelatedObjects(ProjectAllocationId id,
	                                                                                 ProjectId projectId);

	Optional<ProjectAllocationResolved> findByIdWithRelatedObjects(CommunityId communityId, ProjectAllocationId id);

	Set<String> getOccupiedNames(CommunityId communityId, ProjectId id);

	BigDecimal getAvailableAmount(CommunityId communityId, CommunityAllocationId communityAllocationId);

	Set<ProjectAllocation> findAll(CommunityId communityId, ProjectId projectId);

	Set<ProjectAllocationResolved> findAllWithRelatedObjects(CommunityId communityId, ProjectId projectId);

	Set<ProjectDeallocation> findAllUninstallations(ProjectId projectId);

	Set<ProjectAllocationChunk> findAllChunks(ProjectId projectId);

	Set<ProjectAllocationChunk> findAllChunks(ProjectId projectId, ProjectAllocationId projectAllocationId);

	Set<ProjectAllocationChunkResolved> findAllChunksBySiteId(SiteId siteId);

	Set<ProjectAllocationChunkResolved> findAllChunksBySiteIdAndProjectId(SiteId siteId, ProjectId projectId);

	Set<ProjectAllocationInstallation> findAllInstallations(ProjectId projectId);

	Set<ProjectAllocationResolved> findAllWithRelatedObjects(ProjectId projectId);

	Set<ProjectAllocationResolved> findAllWithRelatedObjectsBySiteId(SiteId siteId);

	Set<ProjectAllocationResolved> findAllWithRelatedObjectsBySiteIdAndProjectId(SiteId siteId, ProjectId projectId);

	void create(CommunityId communityId, ProjectAllocation projectAllocation);

	void update(CommunityId communityId, ProjectAllocation projectAllocation);

	void delete(CommunityId communityId, ProjectAllocationId id);
}
