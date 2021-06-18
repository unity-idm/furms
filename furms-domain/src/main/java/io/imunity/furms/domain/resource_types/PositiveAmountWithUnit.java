/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.domain.resource_types;

import java.math.BigDecimal;
import java.util.Objects;

public class PositiveAmountWithUnit {
	public final BigDecimal amount;
	public final ResourceMeasureUnit unit;

	private PositiveAmountWithUnit(BigDecimal amount, ResourceMeasureUnit unit) {
		this.amount = amount;
		this.unit = unit;
	}

	public static PositiveAmountWithUnit roundToPositiveValue(BigDecimal amount, ResourceMeasureUnit unit){
		if(amount.compareTo(BigDecimal.ZERO) < 0)
			return new PositiveAmountWithUnit(BigDecimal.ZERO, unit);
		else
			return new PositiveAmountWithUnit(amount, unit);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		PositiveAmountWithUnit that = (PositiveAmountWithUnit) o;
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
