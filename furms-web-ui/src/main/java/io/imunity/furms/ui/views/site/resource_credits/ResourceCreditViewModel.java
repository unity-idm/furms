/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.views.site.resource_credits;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.Objects;

class ResourceCreditViewModel {
	private final String id;
	private final String siteId;
	private String resourceTypeId;
	private String name;
	private Boolean split = true;
	private BigDecimal amount;
	private ZonedDateTime createTime;
	private ZonedDateTime startTime;
	private ZonedDateTime endTime;

	public ResourceCreditViewModel(String id,
			String siteId,
			String resourceTypeId,
			String name,
			Boolean split,
			BigDecimal amount,
			ZonedDateTime createTime,
			ZonedDateTime startTime,
			ZonedDateTime endTime) {
		this.id = id;
		this.siteId = siteId;
		this.resourceTypeId = resourceTypeId;
		this.name = name;
		this.split = split;
		this.amount = amount;
		this.createTime = createTime;
		this.startTime = startTime;
		this.endTime = endTime;
	}

	public ResourceCreditViewModel(String siteId) {
		this.id = null;
		this.resourceTypeId = null;
		this.siteId = siteId;
	}

	public String getId() {
		return id;
	}

	public String getSiteId() {
		return siteId;
	}

	public String getResourceTypeId() {
		return resourceTypeId;
	}

	public void setResourceTypeId(String resourceTypeId) {
		this.resourceTypeId = resourceTypeId;
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

	public BigDecimal getAmount() {
		return amount;
	}

	public void setAmount(BigDecimal amount) {
		this.amount = amount;
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
			", name='" + name + '\'' +
			", split=" + split +
			", amount=" + amount +
			", startTime=" + startTime +
			", endTime=" + endTime +
			'}';
	}

	public static ResourceCreditViewModelBuilder builder() {
		return new ResourceCreditViewModelBuilder();
	}

	public static final class ResourceCreditViewModelBuilder {
		private  String id;
		private String siteId;
		private String resourceTypeId;
		private String name;
		private Boolean split;
		private BigDecimal amount;
		private ZonedDateTime createTime;
		private ZonedDateTime startTime;
		private ZonedDateTime endTime;

		private ResourceCreditViewModelBuilder() {
		}

		public ResourceCreditViewModelBuilder id(String id) {
			this.id = id;
			return this;
		}

		public ResourceCreditViewModelBuilder siteId(String siteId) {
			this.siteId = siteId;
			return this;
		}

		public ResourceCreditViewModelBuilder resourceTypeId(String resourceTypeId) {
			this.resourceTypeId = resourceTypeId;
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
			return new ResourceCreditViewModel(id, siteId, resourceTypeId, name, split, amount, createTime, startTime,
					endTime);
		}
	}
}