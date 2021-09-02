/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */
package io.imunity.furms.rest.admin;

import io.imunity.furms.domain.community_allocation.CommunityAllocationResolved;

import java.math.BigDecimal;
import java.util.Objects;

import static java.util.Optional.ofNullable;

class CommunityAllocation {
	public final String id;
	public final String creditId;
	public final String name;
	public final BigDecimal amount;

	CommunityAllocation(String id, String creditId, String name, BigDecimal amount) {
		this.id = id;
		this.creditId = creditId;
		this.name = name;
		this.amount = amount;
	}

	CommunityAllocation(CommunityAllocationResolved communityAllocation) {
		this(communityAllocation.id,
				communityAllocation.resourceCredit.id,
				communityAllocation.name,
				ofNullable(communityAllocation.remaining).orElse(communityAllocation.amount));
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		CommunityAllocation that = (CommunityAllocation) o;
		return Objects.equals(id, that.id)
				&& Objects.equals(creditId, that.creditId)
				&& Objects.equals(name, that.name)
				&& Objects.equals(amount, that.amount);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, creditId, name, amount);
	}

	@Override
	public String toString() {
		return "CommunityAllocation{" +
				"id=" + id +
				", creditId='" + creditId + '\'' +
				", name='" + name + '\'' +
				", amount=" + amount +
				'}';
	}
}
