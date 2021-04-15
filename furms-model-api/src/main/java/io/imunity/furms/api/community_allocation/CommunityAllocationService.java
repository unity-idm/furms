/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.api.community_allocation;

import io.imunity.furms.domain.community_allocation.CommunityAllocation;
import io.imunity.furms.domain.community_allocation.CommunityAllocationResolved;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.Set;

public interface CommunityAllocationService {
	Optional<CommunityAllocation> findById(String id);

	Optional<CommunityAllocationResolved> findByIdWithRelatedObjects(String id);

	Set<CommunityAllocation> findAll();

	Set<CommunityAllocationResolved> findAllWithRelatedObjects(String communityId);

	BigDecimal getAvailableAmount(String resourceCreditId);

	void create(CommunityAllocation resourceType);

	void update(CommunityAllocation resourceType);

	void delete(String id);
}
