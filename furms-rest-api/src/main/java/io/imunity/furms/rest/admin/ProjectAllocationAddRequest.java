/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */
package io.imunity.furms.rest.admin;

import java.math.BigDecimal;
import java.util.Objects;

class ProjectAllocationAddRequest {
	public final String communityAllocationId;
	public final String communityId;
	public final String name;
	public final String resourceTypeId;
	public final BigDecimal amount;

	public ProjectAllocationAddRequest(String communityAllocationId, String communityId, String name,
	                                   String resourceTypeId, BigDecimal amount) {
		this.communityAllocationId = communityAllocationId;
		this.communityId = communityId;
		this.name = name;
		this.resourceTypeId = resourceTypeId;
		this.amount = amount;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		ProjectAllocationAddRequest that = (ProjectAllocationAddRequest) o;
		return Objects.equals(communityAllocationId, that.communityAllocationId)
				&& Objects.equals(communityId, that.communityId)
				&& Objects.equals(name, that.name)
				&& Objects.equals(resourceTypeId, that.resourceTypeId)
				&& Objects.equals(amount, that.amount);
	}

	@Override
	public int hashCode() {
		return Objects.hash(communityAllocationId, communityId, name, resourceTypeId, amount);
	}

	@Override
	public String toString() {
		return "ProjectAllocationAddRequest{" +
				"communityAllocationId='" + communityAllocationId + '\'' +
				", communityId='" + communityId + '\'' +
				", name='" + name + '\'' +
				", resourceTypeId='" + resourceTypeId + '\'' +
				", amount=" + amount +
				'}';
	}
}
