/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.db.project_allocation;

import java.math.BigDecimal;
import java.util.Objects;

public class ProjectAllocationSum {
	public final BigDecimal communityAllocationAmount;
	public final BigDecimal projectAllocationsAmount;

	ProjectAllocationSum(BigDecimal communityAllocationAmount, BigDecimal projectAllocationsAmount) {
		this.communityAllocationAmount = communityAllocationAmount;
		this.projectAllocationsAmount = projectAllocationsAmount;
	}

	public BigDecimal getAmount(){
		if(projectAllocationsAmount == null)
			return communityAllocationAmount;
		return communityAllocationAmount.subtract(projectAllocationsAmount);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		ProjectAllocationSum that = (ProjectAllocationSum) o;
		return Objects.equals(communityAllocationAmount, that.communityAllocationAmount) && Objects.equals(projectAllocationsAmount, that.projectAllocationsAmount);
	}

	@Override
	public int hashCode() {
		return Objects.hash(communityAllocationAmount, projectAllocationsAmount);
	}

	@Override
	public String toString() {
		return "ProjectAllocationSum{" +
			"communityAllocationAmount=" + communityAllocationAmount +
			", projectAllocationsAmount=" + projectAllocationsAmount +
			'}';
	}
}
