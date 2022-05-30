/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.api.community_allocation;

import io.imunity.furms.domain.communities.CommunityId;
import io.imunity.furms.domain.community_allocation.CommunityAllocation;
import io.imunity.furms.domain.community_allocation.CommunityAllocationId;
import io.imunity.furms.domain.community_allocation.CommunityAllocationResolved;
import io.imunity.furms.domain.resource_credits.ResourceCreditId;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.Set;

public interface CommunityAllocationService {
	Optional<CommunityAllocation> findById(CommunityAllocationId id);

	Optional<CommunityAllocationResolved> findByCommunityIdAndIdWithRelatedObjects(CommunityId communityId,
	                                                                               CommunityAllocationId id);

	Optional<CommunityAllocationResolved> findByIdWithRelatedObjects(CommunityAllocationId id);

	Set<CommunityAllocation> findAll();

	Set<CommunityAllocation> findAllByCommunityId(CommunityId communityId);

	Set<CommunityAllocationResolved> findAllWithRelatedObjects(CommunityId communityId);

	Set<String> getOccupiedNames(CommunityId communityId);

	Set<CommunityAllocationResolved> findAllWithRelatedObjects(CommunityId communityId,
	                                                           String name,
	                                                           boolean includedFullyDistributed,
	                                                           boolean includedExpired);

	Set<CommunityAllocationResolved> findAllNotExpiredByCommunityIdWithRelatedObjects(CommunityId communityId);

	BigDecimal getAvailableAmountForNew(ResourceCreditId resourceCreditId);

	BigDecimal getAvailableAmountForUpdate(ResourceCreditId resourceCreditId, CommunityAllocationId communityAllocationId);

	void create(CommunityAllocation resourceType);

	void update(CommunityAllocation resourceType);

	void delete(CommunityAllocationId id);
}
