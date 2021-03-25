/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.domain.resource_credit_allocation;

import io.imunity.furms.domain.resource_credits.ResourceCredit;
import io.imunity.furms.domain.resource_types.ResourceType;
import io.imunity.furms.domain.sites.Site;

import java.math.BigDecimal;
import java.util.Objects;

public class ResourceCreditAllocationExtend {

	public final String id;
	public final Site site;
	public final ResourceType resourceType;
	public final ResourceCredit resourceCredit;
	public final String communityId;
	public final String name;
	public final BigDecimal amount;

	ResourceCreditAllocationExtend(String id, Site site, ResourceType resourceType, ResourceCredit resourceCredit,
	                               String communityId, String name, BigDecimal amount) {
		this.id = id;
		this.site = site;
		this.resourceType = resourceType;
		this.resourceCredit = resourceCredit;
		this.communityId = communityId;
		this.name = name;
		this.amount = amount;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		ResourceCreditAllocationExtend that = (ResourceCreditAllocationExtend) o;
		return Objects.equals(id, that.id) &&
			Objects.equals(site, that.site) &&
			Objects.equals(resourceType, that.resourceType) &&
			Objects.equals(resourceCredit, that.resourceCredit) &&
			Objects.equals(communityId, that.communityId) &&
			Objects.equals(name, that.name) &&
			Objects.equals(amount, that.amount);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, site, resourceType, resourceCredit, communityId, name, amount);
	}

	@Override
	public String toString() {
		return "ResourceCreditAllocation{" +
			"id=" + id +
			", site=" + site +
			", resourceType=" + resourceType +
			", resourceCredit=" + resourceCredit +
			", communityId=" + communityId +
			", name='" + name + '\'' +
			", amount='" + amount + '\'' +
			'}';
	}

	public static ResourceCreditAllocationExtendBuilder builder() {
		return new ResourceCreditAllocationExtendBuilder();
	}

	public static final class ResourceCreditAllocationExtendBuilder {
		protected String id;
		public Site site;
		public ResourceType resourceType;
		public ResourceCredit resourceCredit;
		public String communityId;
		public String name;
		public BigDecimal amount;

		private ResourceCreditAllocationExtendBuilder() {
		}

		public ResourceCreditAllocationExtendBuilder site(Site site) {
			this.site = site;
			return this;
		}

		public ResourceCreditAllocationExtendBuilder resourceType(ResourceType resourceType) {
			this.resourceType = resourceType;
			return this;
		}

		public ResourceCreditAllocationExtendBuilder resourceCredit(ResourceCredit resourceCredit) {
			this.resourceCredit = resourceCredit;
			return this;
		}

		public ResourceCreditAllocationExtendBuilder name(String name) {
			this.name = name;
			return this;
		}

		public ResourceCreditAllocationExtendBuilder id(String id) {
			this.id = id;
			return this;
		}

		public ResourceCreditAllocationExtendBuilder communityId(String communityId) {
			this.communityId = communityId;
			return this;
		}

		public ResourceCreditAllocationExtendBuilder amount(BigDecimal amount) {
			this.amount = amount;
			return this;
		}

		public ResourceCreditAllocationExtend build() {
			return new ResourceCreditAllocationExtend(id, site, resourceType, resourceCredit, communityId, name, amount);
		}
	}
}
