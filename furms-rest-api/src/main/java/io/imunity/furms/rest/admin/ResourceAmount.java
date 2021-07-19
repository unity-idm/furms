/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */
package io.imunity.furms.rest.admin;

import java.math.BigDecimal;
import java.util.Objects;

class ResourceAmount {
	public final BigDecimal amount;
	public final String unit;

	ResourceAmount(BigDecimal amount, String unit) {
		this.amount = amount;
		this.unit = unit;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		ResourceAmount that = (ResourceAmount) o;
		return Objects.equals(amount, that.amount)
				&& Objects.equals(unit, that.unit);
	}

	@Override
	public int hashCode() {
		return Objects.hash(amount, unit);
	}

	@Override
	public String toString() {
		return "ResourceAmount{" +
				"amount=" + amount +
				", unit='" + unit + '\'' +
				'}';
	}
}
