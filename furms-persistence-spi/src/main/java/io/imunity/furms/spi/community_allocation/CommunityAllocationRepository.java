/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.spi.community_allocation;

import io.imunity.furms.domain.communities.CommunityId;
import io.imunity.furms.domain.community_allocation.CommunityAllocation;
import io.imunity.furms.domain.community_allocation.CommunityAllocationId;
import io.imunity.furms.domain.community_allocation.CommunityAllocationResolved;
import io.imunity.furms.domain.resource_credits.ResourceCreditId;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.Set;

public interface CommunityAllocationRepository {
	Optional<CommunityAllocation> findById(CommunityAllocationId id);

	Optional<CommunityAllocationResolved> findByIdWithRelatedObjects(CommunityAllocationId id);

	Set<CommunityAllocationResolved> findAllByCommunityIdWithRelatedObjects(CommunityId communityId);

	Set<CommunityAllocation> findAllByCommunityId(CommunityId communityId);

	Set<CommunityAllocationResolved> findAllNotExpiredByCommunityIdWithRelatedObjects(CommunityId communityId);

	Set<CommunityAllocationResolved> findAllByCommunityIdAndNameOrSiteNameWithRelatedObjects(CommunityId communityId,
	                                                                                         String name);

	Set<CommunityAllocationResolved> findAllNotExpiredByCommunityIdAndNameOrSiteNameWithRelatedObjects(CommunityId communityId,
	                                                                                                   String name);

	BigDecimal getAvailableAmount(ResourceCreditId resourceCreditId);

	Set<CommunityAllocation> findAll();

	CommunityAllocationId create(CommunityAllocation resourceType);

	void update(CommunityAllocation resourceType);

	boolean exists(CommunityAllocationId id);

	boolean existsByResourceCreditId(ResourceCreditId id);

	boolean isUniqueName(String name);

	void delete(CommunityAllocationId id);

	void deleteAll();
}

