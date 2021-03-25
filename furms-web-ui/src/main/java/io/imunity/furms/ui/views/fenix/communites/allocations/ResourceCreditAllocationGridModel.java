/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.views.fenix.communites.allocations;

import java.math.BigDecimal;
import java.util.Objects;

class ResourceCreditAllocationGridModel {
	public String id;
	public String siteName;
	public String resourceTypeName;
	public String resourceTypeUnit;
	public String resourceCreditName;
	public String name;
	public BigDecimal amount;

	ResourceCreditAllocationGridModel(String id, String siteName, String resourceTypeName, String resourceTypeUnit, String resourceCreditName, String name, BigDecimal amount) {
		this.id = id;
		this.siteName = siteName;
		this.resourceTypeName = resourceTypeName;
		this.resourceTypeUnit = resourceTypeUnit;
		this.resourceCreditName = resourceCreditName;
		this.name = name;
		this.amount = amount;
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

	String getResourceTypeUnit() {
		return resourceTypeUnit;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		ResourceCreditAllocationGridModel that = (ResourceCreditAllocationGridModel) o;
		return Objects.equals(id, that.id);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id);
	}

	@Override
	public String toString() {
		return "ResourceCreditAllocationGridModel{" +
			"id='" + id + '\'' +
			", siteName='" + siteName + '\'' +
			", resourceTypeName='" + resourceTypeName + '\'' +
			", resourceTypeUnit='" + resourceTypeUnit + '\'' +
			", resourceCreditName='" + resourceCreditName + '\'' +
			", name='" + name + '\'' +
			", amount=" + amount +
			'}';
	}

	public static ResourceCreditAllocationGridModelBuilder builder() {
		return new ResourceCreditAllocationGridModelBuilder();
	}

	public static final class ResourceCreditAllocationGridModelBuilder {
		public String id;
		public String siteName;
		public String resourceTypeName;
		public String resourceTypeUnit;
		public String resourceCreditName;
		public String name;
		public BigDecimal amount;

		private ResourceCreditAllocationGridModelBuilder() {
		}

		public ResourceCreditAllocationGridModelBuilder id(String id) {
			this.id = id;
			return this;
		}

		public ResourceCreditAllocationGridModelBuilder siteName(String siteName) {
			this.siteName = siteName;
			return this;
		}

		public ResourceCreditAllocationGridModelBuilder resourceTypeName(String resourceTypeName) {
			this.resourceTypeName = resourceTypeName;
			return this;
		}

		public ResourceCreditAllocationGridModelBuilder resourceTypeUnit(String resourceTypeUnit) {
			this.resourceTypeUnit = resourceTypeUnit;
			return this;
		}

		public ResourceCreditAllocationGridModelBuilder resourceCreditName(String resourceCreditName) {
			this.resourceCreditName = resourceCreditName;
			return this;
		}

		public ResourceCreditAllocationGridModelBuilder name(String name) {
			this.name = name;
			return this;
		}

		public ResourceCreditAllocationGridModelBuilder amount(BigDecimal amount) {
			this.amount = amount;
			return this;
		}

		public ResourceCreditAllocationGridModel build() {
			return new ResourceCreditAllocationGridModel(id, siteName, resourceTypeName, resourceTypeUnit, resourceCreditName, name, amount);
		}
	}
}
