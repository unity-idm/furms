/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.domain.resource_usage;

import io.imunity.furms.domain.resource_credits.ResourceCreditId;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Optional;

public class ResourceUsageByCredit {
	private final Map<ResourceCreditId, BigDecimal> idToSum;

	public ResourceUsageByCredit(Map<ResourceCreditId, BigDecimal> idToSum) {
		this.idToSum = Map.copyOf(idToSum);
	}

	public BigDecimal get(ResourceCreditId id){
		return Optional.ofNullable(idToSum.get(id))
			.orElse(BigDecimal.ZERO);
	}
}
