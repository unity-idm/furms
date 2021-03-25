/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.domain.resource_credit_allocation;

import java.math.BigDecimal;
import java.util.Objects;

public class ResourceCreditAllocation {

	public final String id;
	public final String siteId;
	public final String communityId;
	public final String resourceTypeId;
	public final String resourceCreditId;
	public final String name;
	public final BigDecimal amount;

	private ResourceCreditAllocation(String id, String siteId, String communityId, String resourceTypeId,
	                         String resourceCreditId, String name, BigDecimal amount) {
		this.id = id;
		this.siteId = siteId;
		this.communityId = communityId;
		this.resourceTypeId = resourceTypeId;
		this.resourceCreditId = resourceCreditId;
		this.name = name;
		this.amount = amount;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		ResourceCreditAllocation that = (ResourceCreditAllocation) o;
		return Objects.equals(id, that.id) &&
			Objects.equals(siteId, that.siteId) &&
			Objects.equals(communityId, that.communityId) &&
			Objects.equals(resourceTypeId, that.resourceTypeId) &&
			Objects.equals(resourceCreditId, that.resourceCreditId) &&
			Objects.equals(name, that.name) &&
			Objects.equals(amount, that.amount);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, siteId, communityId, resourceTypeId, resourceCreditId, name, amount);
	}

	@Override
	public String toString() {
		return "ResourceCreditAllocation{" +
			"id=" + id +
			", siteId=" + siteId +
			", communityId=" + communityId +
			", resourceTypeId=" + resourceTypeId +
			", resourceCreditId=" + resourceCreditId +
			", name='" + name + '\'' +
			", amount='" + amount + '\'' +
			'}';
	}

	public static ResourceCreditAllocationBuilder builder() {
		return new ResourceCreditAllocationBuilder();
	}

	public static final class ResourceCreditAllocationBuilder {
		protected String id;
		public String siteId;
		public String communityId;
		public String resourceTypeId;
		public String resourceCreditId;
		public String name;
		public BigDecimal amount;

		private ResourceCreditAllocationBuilder() {
		}

		public ResourceCreditAllocationBuilder siteId(String siteId) {
			this.siteId = siteId;
			return this;
		}

		public ResourceCreditAllocationBuilder communityId(String communityId) {
			this.communityId = communityId;
			return this;
		}

		public ResourceCreditAllocationBuilder resourceTypeId(String resourceTypeId) {
			this.resourceTypeId = resourceTypeId;
			return this;
		}

		public ResourceCreditAllocationBuilder resourceCreditId(String resourceCreditId) {
			this.resourceCreditId = resourceCreditId;
			return this;
		}

		public ResourceCreditAllocationBuilder name(String name) {
			this.name = name;
			return this;
		}

		public ResourceCreditAllocationBuilder id(String id) {
			this.id = id;
			return this;
		}

		public ResourceCreditAllocationBuilder amount(BigDecimal amount) {
			this.amount = amount;
			return this;
		}

		public ResourceCreditAllocation build() {
			return new ResourceCreditAllocation(id, siteId, communityId, resourceTypeId, resourceCreditId, name, amount);
		}
	}
}
