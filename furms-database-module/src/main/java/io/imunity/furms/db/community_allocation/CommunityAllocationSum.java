/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.db.community_allocation;

import java.math.BigDecimal;
import java.util.Objects;

public class CommunityAllocationSum {
	public final BigDecimal resourceCreditAmount;
	public final BigDecimal communityAllocationsAmount;

	CommunityAllocationSum(BigDecimal resourceCreditAmount, BigDecimal communityAllocationsAmount) {
		this.resourceCreditAmount = resourceCreditAmount;
		this.communityAllocationsAmount = communityAllocationsAmount;
	}

	public BigDecimal getAmount(){
		if(communityAllocationsAmount == null)
			return resourceCreditAmount;
		return resourceCreditAmount.subtract(communityAllocationsAmount);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		CommunityAllocationSum that = (CommunityAllocationSum) o;
		return Objects.equals(resourceCreditAmount, that.resourceCreditAmount) && Objects.equals(communityAllocationsAmount, that.communityAllocationsAmount);
	}

	@Override
	public int hashCode() {
		return Objects.hash(resourceCreditAmount, communityAllocationsAmount);
	}

	@Override
	public String toString() {
		return "CommunityAllocationSum{" +
			"resourceCreditAmount=" + resourceCreditAmount +
			", communityAllocationsAmount=" + communityAllocationsAmount +
			'}';
	}
}
