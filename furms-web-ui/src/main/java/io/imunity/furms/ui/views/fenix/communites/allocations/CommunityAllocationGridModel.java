/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.views.fenix.communites.allocations;

import io.imunity.furms.domain.community_allocation.CommunityAllocationId;
import io.imunity.furms.domain.resource_types.AmountWithUnit;
import io.imunity.furms.domain.resource_types.ResourceMeasureUnit;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

class CommunityAllocationGridModel {
	public final CommunityAllocationId id;
	public final String siteName;
	public final String resourceTypeName;
	public final String resourceCreditName;
	public final String name;
	public final AmountWithUnit amountWithUnit;
	public final AmountWithUnit distributedWithUnit;
	public final AmountWithUnit remainingWithUnit;
	public final BigDecimal consumed;
	public final LocalDateTime creationTime;
	public final LocalDateTime validFrom;
	public final LocalDateTime validTo;

	CommunityAllocationGridModel(CommunityAllocationId id, String siteName, String resourceTypeName,
	                             ResourceMeasureUnit resourceTypeUnit, String resourceCreditName,
	                             String name, BigDecimal amount, BigDecimal remaining, BigDecimal consumed,
	                             LocalDateTime creationTime, LocalDateTime validFrom, LocalDateTime validTo) {
		this.id = id;
		this.siteName = siteName;
		this.resourceTypeName = resourceTypeName;
		this.resourceCreditName = resourceCreditName;
		this.name = name;
		this.amountWithUnit = new AmountWithUnit(amount, resourceTypeUnit);
		this.distributedWithUnit = new AmountWithUnit(amount.subtract(remaining), resourceTypeUnit);
		this.remainingWithUnit = new AmountWithUnit(remaining, resourceTypeUnit);
		this.consumed = consumed;
		this.creationTime = creationTime;
		this.validFrom = validFrom;
		this.validTo = validTo;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		CommunityAllocationGridModel that = (CommunityAllocationGridModel) o;
		return Objects.equals(id, that.id);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id);
	}

	@Override
	public String toString() {
		return "CommunityAllocationGridModel{" +
			"id='" + id + '\'' +
			", siteName='" + siteName + '\'' +
			", resourceTypeName='" + resourceTypeName + '\'' +
			", resourceCreditName='" + resourceCreditName + '\'' +
			", name='" + name + '\'' +
			", amountWithUnit=" + amountWithUnit +
			", distributedWithUnit=" + distributedWithUnit +
			", remainingWithUnit=" + remainingWithUnit +
			", consumed=" + consumed +
			", creationTime=" + creationTime +
			", validFrom=" + validFrom +
			", validTo=" + validTo +
			'}';
	}

	public static CommunityAllocationGridModelBuilder builder() {
		return new CommunityAllocationGridModelBuilder();
	}

	public static final class CommunityAllocationGridModelBuilder {
		private CommunityAllocationId id;
		private String siteName;
		private String resourceTypeName;
		private ResourceMeasureUnit resourceTypeUnit;
		private String resourceCreditName;
		private String name;
		private BigDecimal amount;
		private BigDecimal remaining;
		private BigDecimal consumed;
		private LocalDateTime creationTime;
		private LocalDateTime validFrom;
		private LocalDateTime validTo;

		private CommunityAllocationGridModelBuilder() {
		}

		public CommunityAllocationGridModelBuilder id(CommunityAllocationId id) {
			this.id = id;
			return this;
		}

		public CommunityAllocationGridModelBuilder siteName(String siteName) {
			this.siteName = siteName;
			return this;
		}

		public CommunityAllocationGridModelBuilder resourceTypeName(String resourceTypeName) {
			this.resourceTypeName = resourceTypeName;
			return this;
		}

		public CommunityAllocationGridModelBuilder resourceTypeUnit(ResourceMeasureUnit resourceTypeUnit) {
			this.resourceTypeUnit = resourceTypeUnit;
			return this;
		}

		public CommunityAllocationGridModelBuilder resourceCreditName(String resourceCreditName) {
			this.resourceCreditName = resourceCreditName;
			return this;
		}

		public CommunityAllocationGridModelBuilder name(String name) {
			this.name = name;
			return this;
		}

		public CommunityAllocationGridModelBuilder amount(BigDecimal amount) {
			this.amount = amount;
			return this;
		}

		public CommunityAllocationGridModelBuilder remaining(BigDecimal remaining) {
			this.remaining = remaining;
			return this;
		}

		public CommunityAllocationGridModelBuilder consumed(BigDecimal consumed) {
			this.consumed = consumed;
			return this;
		}

		public CommunityAllocationGridModelBuilder creationTime(LocalDateTime creationTime) {
			this.creationTime = creationTime;
			return this;
		}

		public CommunityAllocationGridModelBuilder validFrom(LocalDateTime validFrom) {
			this.validFrom = validFrom;
			return this;
		}

		public CommunityAllocationGridModelBuilder validTo(LocalDateTime validTo) {
			this.validTo = validTo;
			return this;
		}

		public CommunityAllocationGridModel build() {
			return new CommunityAllocationGridModel(id, siteName, resourceTypeName, resourceTypeUnit, resourceCreditName, name, amount, remaining, consumed, creationTime, validFrom, validTo);
		}
	}
}
