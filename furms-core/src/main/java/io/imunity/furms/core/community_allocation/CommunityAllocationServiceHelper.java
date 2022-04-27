/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.core.community_allocation;

import io.imunity.furms.domain.resource_credits.ResourceCreditId;
import io.imunity.furms.spi.community_allocation.CommunityAllocationRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class CommunityAllocationServiceHelper {
	private final CommunityAllocationRepository communityAllocationRepository;

	CommunityAllocationServiceHelper(CommunityAllocationRepository communityAllocationRepository) {
		this.communityAllocationRepository = communityAllocationRepository;
	}

	public boolean existsByResourceCreditId(ResourceCreditId resourceCreditId) {
		return communityAllocationRepository.existsByResourceCreditId(resourceCreditId);
	}

	public BigDecimal getAvailableAmountForNew(ResourceCreditId resourceCreditId) {
		return communityAllocationRepository.getAvailableAmount(resourceCreditId);
	}
}
