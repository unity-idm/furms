/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.domain.resource_types;

import java.math.BigDecimal;
import java.util.Objects;

public class AmountWithUnit {
	public final BigDecimal amount;
	public final ResourceMeasureUnit unit;

	public AmountWithUnit(BigDecimal amount, ResourceMeasureUnit unit) {
		this.amount = amount;
		this.unit = unit;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		AmountWithUnit that = (AmountWithUnit) o;
		return Objects.equals(amount, that.amount) && unit == that.unit;
	}

	@Override
	public int hashCode() {
		return Objects.hash(amount, unit);
	}

	@Override
	public String toString() {
		return amount.toPlainString() + " " + unit.getSuffix();
	}
}
