/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.domain.resource_usage;

import io.imunity.furms.domain.community_allocation.CommunityAllocationId;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Optional;

public class ResourceUsageByCommunityAllocation {
	private final Map<CommunityAllocationId, BigDecimal> idToSum;

	public ResourceUsageByCommunityAllocation(Map<CommunityAllocationId, BigDecimal> idToSum) {
		this.idToSum = Map.copyOf(idToSum);
	}

	public BigDecimal get(CommunityAllocationId id){
		return Optional.ofNullable(idToSum.get(id))
			.orElse(BigDecimal.ZERO);
	}
}
