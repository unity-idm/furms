/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.api.resource_credit_allocation;

import io.imunity.furms.domain.resource_credit_allocation.ResourceCreditAllocation;
import io.imunity.furms.domain.resource_credit_allocation.ResourceCreditAllocationExtend;

import java.util.Optional;
import java.util.Set;

public interface ResourceCreditAllocationService {
	Optional<ResourceCreditAllocation> findById(String id);

	Optional<ResourceCreditAllocationExtend> findByIdWithRelatedObjects(String id);

	Set<ResourceCreditAllocation> findAll();

	Set<ResourceCreditAllocationExtend> findAllWithRelatedObjects(String communityId);

	void create(ResourceCreditAllocation resourceType);

	void update(ResourceCreditAllocation resourceType);

	void delete(String id);
}
