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

	Optional<CommunityAllocationResolved> findByCommunityIdAndIdWithRelatedObjects(String communityId, String id);

	Optional<CommunityAllocationResolved> findByIdWithRelatedObjects(String id);

	Set<CommunityAllocation> findAll();

	Set<CommunityAllocation> findAllByCommunityId(String communityId);

	Set<CommunityAllocationResolved> findAllWithRelatedObjects(String communityId);

	Set<CommunityAllocationResolved> findAllWithRelatedObjects(String communityId,
	                                                           String name,
	                                                           boolean includedFullyDistributed,
	                                                           boolean includedExpired);

	Set<CommunityAllocationResolved> findAllNotExpiredByCommunityIdWithRelatedObjects(String communityId);

	BigDecimal getAvailableAmountForNew(String resourceCreditId);

	BigDecimal getAvailableAmountForUpdate(String resourceCreditId, String communityAllocationId);

	void create(CommunityAllocation resourceType);

	void update(CommunityAllocation resourceType);

	void delete(String id);
}
