/*
 * Copyright (c) 2021 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.views.fenix.dashboard;

import io.imunity.furms.domain.resource_types.ResourceMeasureUnit;

import java.math.BigDecimal;
import java.util.Objects;

public class DashboardGridResource {

	private final BigDecimal amount;
	private final ResourceMeasureUnit unit;

	public DashboardGridResource(BigDecimal amount, ResourceMeasureUnit unit) {
		this.amount = amount;
		this.unit = unit;
	}

	public BigDecimal getAmount() {
		return amount;
	}

	public ResourceMeasureUnit getUnit() {
		return unit;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		DashboardGridResource that = (DashboardGridResource) o;
		return Objects.equals(amount, that.amount) &&
				Objects.equals(unit, that.unit);
	}

	@Override
	public int hashCode() {
		return Objects.hash(amount, unit);
	}

	@Override
	public String toString() {
		return "DashboardGridResourceCredit{" +
				"amount=" + amount +
				", unit=" + unit +
				'}';
	}

	public static DashboardGridResourceBuilder builder() {
		return new DashboardGridResourceBuilder();
	}

	public static final class DashboardGridResourceBuilder {
		private BigDecimal amount;
		private ResourceMeasureUnit unit;

		private DashboardGridResourceBuilder() {
		}

		public DashboardGridResourceBuilder amount(BigDecimal amount) {
			this.amount = amount;
			return this;
		}

		public DashboardGridResourceBuilder unit(ResourceMeasureUnit unit) {
			this.unit = unit;
			return this;
		}

		public DashboardGridResource build() {
			return new DashboardGridResource(amount, unit);
		}
	}
}
