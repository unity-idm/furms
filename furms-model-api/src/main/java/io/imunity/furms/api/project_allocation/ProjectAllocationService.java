/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.api.project_allocation;

import io.imunity.furms.domain.community_allocation.CommunityAllocationResolved;
import io.imunity.furms.domain.project_allocation.ProjectAllocation;
import io.imunity.furms.domain.project_allocation.ProjectAllocationResolved;
import io.imunity.furms.domain.project_allocation_installation.ProjectAllocationInstallation;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.Set;

public interface ProjectAllocationService {
	Optional<ProjectAllocation> findByCommunityIdAndId(String communityId, String id);

	Optional<ProjectAllocation> findByProjectIdAndId(String projectId, String id);

	Optional<ProjectAllocationResolved> findByIdWithRelatedObjects(String communityId, String id);

	Set<CommunityAllocationResolved> findCorrelatedCommunityAllocation(String communityId);

	BigDecimal getAvailableAmount(String communityId, String communityAllocationId);

	Set<ProjectAllocation> findAll(String communityId, String projectId);

	Set<ProjectAllocationResolved> findAllWithRelatedObjects(String communityId, String projectId);

	Set<ProjectAllocationInstallation> findAllInstallations(String communityId, String projectId);

	Set<ProjectAllocationInstallation> findAllInstallations(String projectId);

	Set<ProjectAllocationResolved> findAllWithRelatedObjects(String projectId);

	void create(String communityId, ProjectAllocation projectAllocation);

	void update(String communityId, ProjectAllocation projectAllocation);

	void delete(String communityId, String id);
}
