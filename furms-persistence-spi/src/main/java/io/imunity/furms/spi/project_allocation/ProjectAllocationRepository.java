/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.spi.project_allocation;

import io.imunity.furms.domain.project_allocation.ProjectAllocation;
import io.imunity.furms.domain.project_allocation.ProjectAllocationResolved;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.Set;

public interface ProjectAllocationRepository {
	Optional<ProjectAllocation> findById(String id);

	Optional<ProjectAllocationResolved> findByIdWithRelatedObjects(String id);

	Set<ProjectAllocationResolved> findAllWithRelatedObjects(String projectId);

	Set<ProjectAllocationResolved> findAllWithRelatedObjects(String siteId, String projectId);

	Set<ProjectAllocationResolved> findAllWithRelatedObjectsBySiteId(String siteId);

	Set<ProjectAllocation> findAll(String projectId);

	String create(ProjectAllocation projectAllocation);

	String update(ProjectAllocation projectAllocation);

	BigDecimal getAvailableAmount(String communityAllocationId);

	boolean exists(String id);

	boolean existsByCommunityAllocationId(String id);

	boolean isNamePresent(String communityId, String name);

	void deleteById(String id);

	void deleteAll();
}

