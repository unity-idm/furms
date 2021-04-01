/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.spi.project_allocation;

import io.imunity.furms.domain.project_allocation.ProjectAllocation;

import java.util.Optional;
import java.util.Set;

public interface ProjectAllocationRepository {
	Optional<ProjectAllocation> findById(String id);

//	Optional<CommunityAllocationResolved> findByIdWithRelatedObjects(String id);
//
//	Set<CommunityAllocationResolved> findAllWithRelatedObjects(String communityId);

	Set<ProjectAllocation> findAll();

	String create(ProjectAllocation projectAllocation);

	String update(ProjectAllocation projectAllocation);

	boolean exists(String id);

	boolean existsByResourceCreditId(String id);

	boolean isUniqueName(String name);

	void delete(String id);

	void deleteAll();
}

