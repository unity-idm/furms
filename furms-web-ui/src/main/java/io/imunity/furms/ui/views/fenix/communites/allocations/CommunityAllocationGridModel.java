/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.views.fenix.communites.allocations;

import io.imunity.furms.domain.resource_types.AmountWithUnit;
import io.imunity.furms.domain.resource_types.ResourceMeasureUnit;

import java.math.BigDecimal;
import java.util.Objects;

class CommunityAllocationGridModel {
	public String id;
	public String siteName;
	public String resourceTypeName;
	public String resourceCreditName;
	public String name;
	public AmountWithUnit amountWithUnit;

	CommunityAllocationGridModel(String id, String siteName, String resourceTypeName, ResourceMeasureUnit resourceTypeUnit, String resourceCreditName, String name, BigDecimal amount) {
		this.id = id;
		this.siteName = siteName;
		this.resourceTypeName = resourceTypeName;
		this.resourceCreditName = resourceCreditName;
		this.name = name;
		this.amountWithUnit = new AmountWithUnit(amount, resourceTypeUnit);
	}

	String getSiteName() {
		return siteName;
	}

	String getResourceTypeName() {
		return resourceTypeName;
	}

	String getResourceCreditName() {
		return resourceCreditName;
	}

	AmountWithUnit getAmountWithUnit() {
		return amountWithUnit;
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
			'}';
	}

	public static CommunityAllocationGridModelBuilder builder() {
		return new CommunityAllocationGridModelBuilder();
	}

	public static final class CommunityAllocationGridModelBuilder {
		public String id;
		public String siteName;
		public String resourceTypeName;
		public ResourceMeasureUnit resourceTypeUnit;
		public String resourceCreditName;
		public String name;
		public BigDecimal amount;

		private CommunityAllocationGridModelBuilder() {
		}

		public CommunityAllocationGridModelBuilder id(String id) {
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

		public CommunityAllocationGridModel build() {
			return new CommunityAllocationGridModel(id, siteName, resourceTypeName, resourceTypeUnit, resourceCreditName, name, amount);
		}
	}
}
