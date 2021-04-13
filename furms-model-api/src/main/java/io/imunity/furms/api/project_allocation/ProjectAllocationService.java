/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.api.project_allocation;

import io.imunity.furms.domain.project_allocation.ProjectAllocation;
import io.imunity.furms.domain.project_allocation.ProjectAllocationResolved;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.Set;

public interface ProjectAllocationService {
	Optional<ProjectAllocation> findById(String id);

	Optional<ProjectAllocationResolved> findByIdWithRelatedObjects(String id);

	BigDecimal getAvailableAmount(String communityAllocationId);

	Set<ProjectAllocation> findAll();

	Set<ProjectAllocationResolved> findAllWithRelatedObjects(String communityId);

	void create(ProjectAllocation resourceType);

	void update(ProjectAllocation resourceType);

	void delete(String id);
}
