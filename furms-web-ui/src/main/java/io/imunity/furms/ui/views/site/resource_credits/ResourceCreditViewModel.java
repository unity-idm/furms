/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.views.site.resource_credits;

import io.imunity.furms.domain.resource_credits.ResourceCreditId;
import io.imunity.furms.domain.resource_types.AmountWithUnit;
import io.imunity.furms.domain.resource_types.ResourceMeasureUnit;
import io.imunity.furms.domain.resource_types.ResourceTypeId;
import io.imunity.furms.domain.sites.SiteId;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.Objects;
import java.util.Optional;

class ResourceCreditViewModel {
	private final ResourceCreditId id;
	private final SiteId siteId;
	private ResourceTypeId resourceTypeId;
	private String resourceTypeName;
	private String name;
	private Boolean split = true;
	private AmountWithUnit amount;
	private AmountWithUnit distributed;
	private AmountWithUnit remaining;
	private BigDecimal consumed;
	private ZonedDateTime createTime;
	private ZonedDateTime startTime;
	private ZonedDateTime endTime;

	public ResourceCreditViewModel(ResourceCreditId id,
			SiteId siteId,
			ResourceTypeId resourceTypeId,
			String resourceTypeName,
			String name,
			Boolean split,
			BigDecimal amount,
			BigDecimal remaining,
			BigDecimal consumed,
			ResourceMeasureUnit unit,
			ZonedDateTime createTime,
			ZonedDateTime startTime,
			ZonedDateTime endTime) {
		this.id = id;
		this.siteId = siteId;
		this.resourceTypeId = resourceTypeId;
		this.resourceTypeName = resourceTypeName;
		this.name = name;
		this.split = split;
		this.amount = new AmountWithUnit(amount, unit);
		this.distributed = new AmountWithUnit(amount.subtract(remaining), unit);
		this.remaining = new AmountWithUnit(remaining, unit);
		this.consumed = consumed;
		this.createTime = createTime;
		this.startTime = startTime;
		this.endTime = endTime;
	}

	public ResourceCreditViewModel(SiteId siteId) {
		this.id = null;
		this.resourceTypeName = null;
		this.siteId = siteId;
	}

	public ResourceCreditId getId() {
		return id;
	}

	public SiteId getSiteId() {
		return siteId;
	}

	public String getResourceTypeName() {
		return resourceTypeName;
	}

	public ResourceTypeId getResourceTypeId() {
		return resourceTypeId;
	}

	public void setResourceTypeId(ResourceTypeId resourceTypeId) {
		this.resourceTypeId = resourceTypeId;
	}

	public void setResourceTypeName(String resourceTypeName) {
		this.resourceTypeName = resourceTypeName;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Boolean getSplit() {
		return split;
	}

	public void setSplit(Boolean split) {
		this.split = split;
	}

	public AmountWithUnit getAmount() {
		return amount;
	}

	public void setAmount(BigDecimal amount) {
		this.amount = new AmountWithUnit(amount, Optional.ofNullable(this.amount).map(x -> x.unit).orElse(null));
	}

	public ZonedDateTime getStartTime() {
		return startTime;
	}

	public void setStartTime(ZonedDateTime startTime) {
		this.startTime = startTime;
	}

	public ZonedDateTime getEndTime() {
		return endTime;
	}

	public void setEndTime(ZonedDateTime endTime) {
		this.endTime = endTime;
	}

	public ZonedDateTime getCreateTime() {
		return createTime;
	}

	public void setCreateTime(ZonedDateTime createTime) {
		this.createTime = createTime;
	}

	public AmountWithUnit getDistributed() {
		return distributed;
	}

	public AmountWithUnit getRemaining() {
		return remaining;
	}


	public BigDecimal getConsumed() {
		return consumed;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		ResourceCreditViewModel that = (ResourceCreditViewModel) o;
		return Objects.equals(id, that.id);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id);
	}

	@Override
	public String toString() {
		return "ResourceCreditViewModel{" +
			"id='" + id + '\'' +
			", siteId='" + siteId + '\'' +
			", resourceTypeId='" + resourceTypeId + '\'' +
			", resourceTypeName='" + resourceTypeName + '\'' +
			", name='" + name + '\'' +
			", split=" + split +
			", amount=" + amount +
			", distributed=" + distributed +
			", remaining=" + remaining +
			", consumed=" + consumed +
			", startTime=" + startTime +
			", endTime=" + endTime +
			'}';
	}

	public static ResourceCreditViewModelBuilder builder() {
		return new ResourceCreditViewModelBuilder();
	}

	public static final class ResourceCreditViewModelBuilder {
		private ResourceCreditId id;
		private SiteId siteId;
		private ResourceTypeId resourceTypeId;
		private String resourceTypeName;
		private String name;
		private Boolean split;
		private BigDecimal amount;
		private BigDecimal remaining;
		private BigDecimal consumed;
		private ResourceMeasureUnit unit;
		private ZonedDateTime createTime;
		private ZonedDateTime startTime;
		private ZonedDateTime endTime;

		private ResourceCreditViewModelBuilder() {
		}

		public ResourceCreditViewModelBuilder id(ResourceCreditId id) {
			this.id = id;
			return this;
		}

		public ResourceCreditViewModelBuilder siteId(SiteId siteId) {
			this.siteId = siteId;
			return this;
		}

		public ResourceCreditViewModelBuilder resourceTypeId(ResourceTypeId resourceTypeId) {
			this.resourceTypeId = resourceTypeId;
			return this;
		}

		public ResourceCreditViewModelBuilder resourceTypeName(String resourceTypeName) {
			this.resourceTypeName = resourceTypeName;
			return this;
		}

		public ResourceCreditViewModelBuilder name(String name) {
			this.name = name;
			return this;
		}

		public ResourceCreditViewModelBuilder split(Boolean split) {
			this.split = split;
			return this;
		}

		public ResourceCreditViewModelBuilder amount(BigDecimal amount) {
			this.amount = amount;
			return this;
		}

		public ResourceCreditViewModelBuilder remaining(BigDecimal remaining) {
			this.remaining = remaining;
			return this;
		}

		public ResourceCreditViewModelBuilder consumed(BigDecimal consumed) {
			this.consumed = consumed;
			return this;
		}

		public ResourceCreditViewModelBuilder unit(ResourceMeasureUnit unit) {
			this.unit = unit;
			return this;
		}

		public ResourceCreditViewModelBuilder createTime(ZonedDateTime createTime) {
			this.createTime = createTime;
			return this;
		}

		public ResourceCreditViewModelBuilder startTime(ZonedDateTime startTime) {
			this.startTime = startTime;
			return this;
		}

		public ResourceCreditViewModelBuilder endTime(ZonedDateTime endTime) {
			this.endTime = endTime;
			return this;
		}

		public ResourceCreditViewModel build() {
			return new ResourceCreditViewModel(id, siteId, resourceTypeId, resourceTypeName, name, split, amount, remaining, consumed,
				unit, createTime, startTime, endTime);
		}
	}
}
