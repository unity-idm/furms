/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.spi.community_allocation;

import io.imunity.furms.domain.community_allocation.CommunityAllocation;
import io.imunity.furms.domain.community_allocation.CommunityAllocationResolved;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.Set;

public interface CommunityAllocationRepository {
	Optional<CommunityAllocation> findById(String id);

	Optional<CommunityAllocationResolved> findByIdWithRelatedObjects(String id);

	Set<CommunityAllocationResolved> findAllByCommunityIdWithRelatedObjects(String communityId);

	Set<CommunityAllocationResolved> findAllNotExpiredByCommunityIdWithRelatedObjects(String communityId);

	BigDecimal getAvailableAmount(String resourceCreditId);

	Set<CommunityAllocation> findAll();

	String create(CommunityAllocation resourceType);

	String update(CommunityAllocation resourceType);

	boolean exists(String id);

	boolean existsByResourceCreditId(String id);

	boolean isUniqueName(String communityId, String name);

	void delete(String id);

	void deleteAll();
}

