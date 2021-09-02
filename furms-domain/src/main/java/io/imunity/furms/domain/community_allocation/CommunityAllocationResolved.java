/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.domain.community_allocation;

import io.imunity.furms.domain.resource_credits.ResourceCredit;
import io.imunity.furms.domain.resource_types.ResourceType;
import io.imunity.furms.domain.sites.Site;

import java.math.BigDecimal;
import java.util.Objects;

public class CommunityAllocationResolved {

	public final String id;
	public final Site site;
	public final ResourceType resourceType;
	public final ResourceCredit resourceCredit;
	public final String communityId;
	public final String name;
	public final BigDecimal amount;
	public final BigDecimal consumed;
	public final BigDecimal remaining;

	CommunityAllocationResolved(String id, Site site, ResourceType resourceType, ResourceCredit resourceCredit,
	                            String communityId, String name, BigDecimal amount, BigDecimal consumed, BigDecimal remaining) {
		this.id = id;
		this.site = site;
		this.resourceType = resourceType;
		this.resourceCredit = resourceCredit;
		this.communityId = communityId;
		this.name = name;
		this.amount = amount;
		this.consumed = consumed;
		this.remaining = remaining;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		CommunityAllocationResolved that = (CommunityAllocationResolved) o;
		return Objects.equals(id, that.id) &&
				Objects.equals(site, that.site) &&
				Objects.equals(resourceType, that.resourceType) &&
				Objects.equals(resourceCredit, that.resourceCredit) &&
				Objects.equals(communityId, that.communityId) &&
				Objects.equals(name, that.name) &&
				Objects.equals(amount, that.amount) &&
				Objects.equals(consumed, that.consumed) &&
				Objects.equals(remaining, that.remaining);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, site, resourceType, resourceCredit, communityId, name, amount, consumed, remaining);
	}

	@Override
	public String toString() {
		return "CommunityAllocation{" +
				"id=" + id +
				", site=" + site +
				", resourceType=" + resourceType +
				", resourceCredit=" + resourceCredit +
				", communityId=" + communityId +
				", name='" + name + '\'' +
				", amount='" + amount + '\'' +
				", consumed='" + consumed + '\'' +
				", remaining='" + remaining + '\'' +
				'}';
	}

	public static CommunityAllocationResolvedBuilder builder() {
		return new CommunityAllocationResolvedBuilder();
	}

	public CommunityAllocationResolvedBuilder copyBuilder() {
		return new CommunityAllocationResolvedBuilder()
				.id(this.id)
				.site(this.site)
				.resourceType(this.resourceType)
				.resourceCredit(this.resourceCredit)
				.communityId(this.communityId)
				.name(this.name)
				.amount(this.amount)
				.consumed(this.consumed)
				.remaining(this.remaining);
	}

	public static final class CommunityAllocationResolvedBuilder {
		private String id;
		private Site site;
		private ResourceType resourceType;
		private ResourceCredit resourceCredit;
		private String communityId;
		private String name;
		private BigDecimal amount;
		private BigDecimal consumed;
		private BigDecimal remaining;

		private CommunityAllocationResolvedBuilder() {
		}

		public CommunityAllocationResolvedBuilder site(Site site) {
			this.site = site;
			return this;
		}

		public CommunityAllocationResolvedBuilder resourceType(ResourceType resourceType) {
			this.resourceType = resourceType;
			return this;
		}

		public CommunityAllocationResolvedBuilder resourceCredit(ResourceCredit resourceCredit) {
			this.resourceCredit = resourceCredit;
			return this;
		}

		public CommunityAllocationResolvedBuilder name(String name) {
			this.name = name;
			return this;
		}

		public CommunityAllocationResolvedBuilder id(String id) {
			this.id = id;
			return this;
		}

		public CommunityAllocationResolvedBuilder communityId(String communityId) {
			this.communityId = communityId;
			return this;
		}

		public CommunityAllocationResolvedBuilder consumed(BigDecimal consumed) {
			this.consumed = consumed;
			return this;
		}

		public CommunityAllocationResolvedBuilder amount(BigDecimal amount) {
			this.amount = amount;
			return this;
		}

		public CommunityAllocationResolvedBuilder remaining(BigDecimal remaining) {
			this.remaining = remaining;
			return this;
		}

		public CommunityAllocationResolved build() {
			return new CommunityAllocationResolved(id, site, resourceType, resourceCredit, communityId, name, amount, consumed, remaining);
		}
	}
}
