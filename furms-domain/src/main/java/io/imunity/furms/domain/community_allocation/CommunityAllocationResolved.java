/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.domain.community_allocation;

import io.imunity.furms.domain.communities.CommunityId;
import io.imunity.furms.domain.resource_credits.ResourceCredit;
import io.imunity.furms.domain.resource_types.ResourceType;
import io.imunity.furms.domain.sites.Site;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

public class CommunityAllocationResolved {

	public final CommunityAllocationId id;
	public final Site site;
	public final ResourceType resourceType;
	public final ResourceCredit resourceCredit;
	public final CommunityId communityId;
	public final String communityName;
	public final String name;
	public final BigDecimal amount;
	public final BigDecimal consumed;
	public final BigDecimal remaining;
	public final LocalDateTime creationTime;

	CommunityAllocationResolved(CommunityAllocationId id, Site site, ResourceType resourceType, ResourceCredit resourceCredit,
	                            CommunityId communityId, String communityName, String name, BigDecimal amount,
	                            BigDecimal consumed, BigDecimal remaining, LocalDateTime creationTime) {
		this.id = id;
		this.site = site;
		this.resourceType = resourceType;
		this.resourceCredit = resourceCredit;
		this.communityId = communityId;
		this.communityName = communityName;
		this.name = name;
		this.amount = amount;
		this.consumed = consumed;
		this.remaining = remaining;
		this.creationTime = creationTime;
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
				Objects.equals(communityName, that.communityName) &&
				Objects.equals(name, that.name) &&
				Objects.equals(amount, that.amount) &&
				Objects.equals(consumed, that.consumed) &&
				Objects.equals(creationTime, that.creationTime) &&
				Objects.equals(remaining, that.remaining);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, site, resourceType, resourceCredit, communityId, communityName, name, amount, consumed, creationTime, remaining);
	}

	@Override
	public String toString() {
		return "CommunityAllocation{" +
				"id=" + id +
				", site=" + site +
				", resourceType=" + resourceType +
				", resourceCredit=" + resourceCredit +
				", communityId=" + communityId +
				", communityName=" + communityName +
				", name='" + name + '\'' +
				", amount='" + amount + '\'' +
				", consumed='" + consumed + '\'' +
				", remaining='" + remaining + '\'' +
				", creationTime='" + creationTime + '\'' +
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
				.communityName(this.communityName)
				.name(this.name)
				.amount(this.amount)
				.consumed(this.consumed)
				.remaining(this.remaining)
				.creationTime(this.creationTime);
	}

	public static final class CommunityAllocationResolvedBuilder {
		private CommunityAllocationId id;
		private Site site;
		private ResourceType resourceType;
		private ResourceCredit resourceCredit;
		private CommunityId communityId;
		private String communityName;
		private String name;
		private BigDecimal amount;
		private BigDecimal consumed;
		private BigDecimal remaining;
		private LocalDateTime creationTime;

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
			this.id = new CommunityAllocationId(id);
			return this;
		}

		public CommunityAllocationResolvedBuilder id(CommunityAllocationId id) {
			this.id = id;
			return this;
		}

		public CommunityAllocationResolvedBuilder communityId(String communityId) {
			this.communityId = new CommunityId(communityId);
			return this;
		}

		public CommunityAllocationResolvedBuilder communityId(CommunityId communityId) {
			this.communityId = communityId;
			return this;
		}

		public CommunityAllocationResolvedBuilder communityName(String communityName) {
			this.communityName = communityName;
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

		public CommunityAllocationResolvedBuilder creationTime(LocalDateTime creationTime) {
			this.creationTime = creationTime;
			return this;
		}

		public CommunityAllocationResolved build() {
			return new CommunityAllocationResolved(
				id, site, resourceType, resourceCredit, communityId, communityName,
				name, amount, consumed, remaining, creationTime
			);
		}
	}
}
