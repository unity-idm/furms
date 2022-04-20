/*
 * Copyright (c) 2021 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.domain.resource_credits;

import io.imunity.furms.domain.resource_types.ResourceType;
import io.imunity.furms.domain.sites.SiteId;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

public class ResourceCreditWithAllocations {
	private final ResourceCreditId id;
	private final String name;
	private final SiteId siteId;
	private final ResourceType resourceType;
	private final Boolean split;
	private final BigDecimal amount;
	private final BigDecimal remaining;
	private final BigDecimal consumed;
	private final LocalDateTime utcCreateTime;
	private final LocalDateTime utcStartTime;
	private final LocalDateTime utcEndTime;

	public ResourceCreditWithAllocations(ResourceCreditId id,
	                                     String name,
	                                     SiteId siteId,
	                                     ResourceType resourceType,
	                                     Boolean split,
	                                     BigDecimal amount,
	                                     BigDecimal remaining,
	                                     BigDecimal consumed,
	                                     LocalDateTime utcCreateTime,
	                                     LocalDateTime utcStartTime,
	                                     LocalDateTime utcEndTime) {
		this.id = id;
		this.name = name;
		this.siteId = siteId;
		this.resourceType = resourceType;
		this.split = split;
		this.amount = amount;
		this.remaining = remaining;
		this.consumed = consumed;
		this.utcCreateTime = utcCreateTime;
		this.utcStartTime = utcStartTime;
		this.utcEndTime = utcEndTime;
	}

	public ResourceCreditId getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public SiteId getSiteId() {
		return siteId;
	}

	public ResourceType getResourceType() {
		return resourceType;
	}

	public Boolean getSplit() {
		return split;
	}

	public BigDecimal getAmount() {
		return amount;
	}

	public BigDecimal getRemaining() {
		return remaining;
	}

	public BigDecimal getConsumed() {
		return consumed;
	}

	public LocalDateTime getUtcCreateTime() {
		return utcCreateTime;
	}

	public LocalDateTime getUtcStartTime() {
		return utcStartTime;
	}

	public LocalDateTime getUtcEndTime() {
		return utcEndTime;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		ResourceCreditWithAllocations that = (ResourceCreditWithAllocations) o;
		return Objects.equals(id, that.id) &&
				Objects.equals(name, that.name) &&
				Objects.equals(siteId, that.siteId) &&
				Objects.equals(resourceType, that.resourceType) &&
				Objects.equals(split, that.split) &&
				Objects.equals(amount, that.amount) &&
				Objects.equals(remaining, that.remaining) &&
				Objects.equals(consumed, that.consumed) &&
				Objects.equals(utcCreateTime, that.utcCreateTime) &&
				Objects.equals(utcStartTime, that.utcStartTime) &&
				Objects.equals(utcEndTime, that.utcEndTime);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, name, siteId, resourceType, split, amount, remaining, consumed, utcCreateTime, utcStartTime, utcEndTime);
	}

	@Override
	public String toString() {
		return "ResourceCreditWithAllocations{" +
				"id='" + id + '\'' +
				", name='" + name + '\'' +
				", siteId='" + siteId + '\'' +
				", resourceType='" + resourceType + '\'' +
				", split=" + split +
				", amount=" + amount +
				", consumed=" + consumed +
				", remaining=" + remaining +
				", utcCreateTime=" + utcCreateTime +
				", utcStartTime=" + utcStartTime +
				", utcEndTime=" + utcEndTime +
				'}';
	}

	public static ResourceCreditWithAllocationsBuilder builder() {
		return new ResourceCreditWithAllocationsBuilder();
	}

	public static final class ResourceCreditWithAllocationsBuilder {
		public ResourceCreditId id;
		public String name;
		public SiteId siteId;
		public ResourceType resourceType;
		public Boolean split;
		public BigDecimal amount;
		public BigDecimal consumed;
		public BigDecimal remaining;
		public LocalDateTime utcCreateTime;
		public LocalDateTime utcStartTime;
		public LocalDateTime utcEndTime;

		private ResourceCreditWithAllocationsBuilder() {
		}

		public ResourceCreditWithAllocationsBuilder id(String id) {
			this.id = new ResourceCreditId(id);
			return this;
		}

		public ResourceCreditWithAllocationsBuilder name(String name) {
			this.name = name;
			return this;
		}

		public ResourceCreditWithAllocationsBuilder siteId(String siteId) {
			this.siteId = new SiteId(siteId);
			return this;
		}

		public ResourceCreditWithAllocationsBuilder resourceType(ResourceType resourceType) {
			this.resourceType = resourceType;
			return this;
		}

		public ResourceCreditWithAllocationsBuilder split(Boolean split) {
			this.split = split;
			return this;
		}

		public ResourceCreditWithAllocationsBuilder amount(BigDecimal amount) {
			this.amount = amount;
			return this;
		}

		public ResourceCreditWithAllocationsBuilder consumed(BigDecimal consumed) {
			this.consumed = consumed;
			return this;
		}

		public ResourceCreditWithAllocationsBuilder remaining(BigDecimal remaining) {
			this.remaining = remaining;
			return this;
		}

		public ResourceCreditWithAllocationsBuilder utcCreateTime(LocalDateTime utcCreateTime) {
			this.utcCreateTime = utcCreateTime;
			return this;
		}

		public ResourceCreditWithAllocationsBuilder utcStartTime(LocalDateTime utcStartTime) {
			this.utcStartTime = utcStartTime;
			return this;
		}

		public ResourceCreditWithAllocationsBuilder utcEndTime(LocalDateTime utcEndTime) {
			this.utcEndTime = utcEndTime;
			return this;
		}

		public ResourceCreditWithAllocations build() {
			return new ResourceCreditWithAllocations(id, name, siteId, resourceType, split, amount, remaining, consumed,
					utcCreateTime, utcStartTime, utcEndTime);
		}
	}
}
