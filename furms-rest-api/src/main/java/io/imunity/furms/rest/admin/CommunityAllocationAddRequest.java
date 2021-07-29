/*
 * Copyright (c) 2021 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */
package io.imunity.furms.rest.admin;

import java.math.BigDecimal;
import java.util.Objects;

class CommunityAllocationAddRequest {
	public final String creditId;
	public final String name;
	public final BigDecimal amount;

	public CommunityAllocationAddRequest(String creditId, String name, BigDecimal amount) {
		this.creditId = creditId;
		this.name = name;
		this.amount = amount;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		CommunityAllocationAddRequest that = (CommunityAllocationAddRequest) o;
		return Objects.equals(creditId, that.creditId)
				&& Objects.equals(name, that.name)
				&& Objects.equals(amount, that.amount);
	}

	@Override
	public int hashCode() {
		return Objects.hash(creditId, name, amount);
	}

	@Override
	public String toString() {
		return "CommunityAllocationAddRequest{" +
				"creditId='" + creditId + '\'' +
				", name='" + name + '\'' +
				", amount=" + amount +
				'}';
	}
}
