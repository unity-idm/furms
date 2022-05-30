/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.spi.project_allocation;

import io.imunity.furms.domain.communities.CommunityId;
import io.imunity.furms.domain.community_allocation.CommunityAllocationId;
import io.imunity.furms.domain.project_allocation.ProjectAllocation;
import io.imunity.furms.domain.project_allocation.ProjectAllocationId;
import io.imunity.furms.domain.project_allocation.ProjectAllocationResolved;
import io.imunity.furms.domain.projects.ProjectId;
import io.imunity.furms.domain.sites.SiteId;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.Set;

public interface ProjectAllocationRepository {
	Optional<ProjectAllocation> findById(ProjectAllocationId id);

	Optional<ProjectAllocationResolved> findByIdWithRelatedObjects(ProjectAllocationId id);

	Set<ProjectAllocationResolved> findAllWithRelatedObjects(ProjectId projectId);

	Set<ProjectAllocationResolved> findAllWithRelatedObjects(SiteId siteId, ProjectId projectId);

	Set<ProjectAllocationResolved> findAllWithRelatedObjectsBySiteId(SiteId siteId);

	Set<ProjectAllocation> findAll(ProjectId projectId);

	ProjectAllocationId create(ProjectAllocation projectAllocation);

	void update(ProjectAllocation projectAllocation);

	BigDecimal getAvailableAmount(CommunityAllocationId communityAllocationId);

	boolean exists(ProjectAllocationId id);

	boolean existsByCommunityAllocationId(CommunityAllocationId id);

	boolean isNamePresent(CommunityId communityId, String name);

	void deleteById(ProjectAllocationId id);

	void deleteAll();
}

