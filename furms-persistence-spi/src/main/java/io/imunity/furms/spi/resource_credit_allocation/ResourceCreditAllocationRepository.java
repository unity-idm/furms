/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.spi.resource_credit_allocation;

import io.imunity.furms.domain.resource_credit_allocation.ResourceCreditAllocation;
import io.imunity.furms.domain.resource_credit_allocation.ResourceCreditAllocationExtend;

import java.util.Optional;
import java.util.Set;

public interface ResourceCreditAllocationRepository {
	Optional<ResourceCreditAllocation> findById(String id);

	Optional<ResourceCreditAllocationExtend> findByIdWithRelatedObjects(String id);

	Set<ResourceCreditAllocationExtend> findAllWithRelatedObjects(String communityId);

	Set<ResourceCreditAllocation> findAll();

	String create(ResourceCreditAllocation resourceType);

	String update(ResourceCreditAllocation resourceType);

	boolean exists(String id);

	boolean existsByResourceCreditId(String id);

	boolean isUniqueName(String name);

	void delete(String id);

	void deleteAll();
}

