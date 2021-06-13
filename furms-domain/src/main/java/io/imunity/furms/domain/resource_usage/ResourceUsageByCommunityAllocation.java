/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.domain.resource_usage;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Optional;

public class ResourceUsageByCommunityAllocation {
	private final Map<String, BigDecimal> idToSum;

	public ResourceUsageByCommunityAllocation(Map<String, BigDecimal> idToSum) {
		this.idToSum = Map.copyOf(idToSum);
	}

	public BigDecimal get(String id){
		return Optional.ofNullable(idToSum.get(id))
			.orElse(BigDecimal.ZERO);
	}
}
