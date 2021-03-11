/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.views.site.resource_credits;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

class ResourceCreditViewModel {
	public final String id;
	public final String siteId;
	public String resourceTypeId;
	public String name;
	public Boolean split = true;
	public Boolean access = false;
	public BigDecimal amount;
	public LocalDateTime createTime;
	public LocalDateTime startTime;
	public LocalDateTime endTime;

	public ResourceCreditViewModel(String id, String siteId, String resourceTypeId, String name, Boolean split,
	                               Boolean access, BigDecimal amount, LocalDateTime createTime, LocalDateTime startTime, LocalDateTime endTime) {
		this.id = id;
		this.siteId = siteId;
		this.resourceTypeId = resourceTypeId;
		this.name = name;
		this.split = split;
		this.access = access;
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

	public Boolean getAccess() {
		return access;
	}

	public void setAccess(Boolean access) {
		this.access = access;
	}

	public BigDecimal getAmount() {
		return amount;
	}

	public void setAmount(BigDecimal amount) {
		this.amount = amount;
	}

	public LocalDateTime getStartTime() {
		return startTime;
	}

	public void setStartTime(LocalDateTime startTime) {
		this.startTime = startTime;
	}

	public LocalDateTime getEndTime() {
		return endTime;
	}

	public void setEndTime(LocalDateTime endTime) {
		this.endTime = endTime;
	}

	public LocalDateTime getCreateTime() {
		return createTime;
	}

	public void setCreateTime(LocalDateTime createTime) {
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
			", access=" + access +
			", amount=" + amount +
			", startTime=" + startTime +
			", endTime=" + endTime +
			'}';
	}

	public static ResourceCreditViewModelBuilder builder() {
		return new ResourceCreditViewModelBuilder();
	}

	public static final class ResourceCreditViewModelBuilder {
		public String id;
		public String siteId;
		public String resourceTypeId;
		public String name;
		public Boolean split;
		public Boolean access;
		public BigDecimal amount;
		public LocalDateTime createTime;
		public LocalDateTime startTime;
		public LocalDateTime endTime;

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

		public ResourceCreditViewModelBuilder access(Boolean access) {
			this.access = access;
			return this;
		}

		public ResourceCreditViewModelBuilder amount(BigDecimal amount) {
			this.amount = amount;
			return this;
		}

		public ResourceCreditViewModelBuilder createTime(LocalDateTime createTime) {
			this.createTime = createTime;
			return this;
		}

		public ResourceCreditViewModelBuilder startTime(LocalDateTime startTime) {
			this.startTime = startTime;
			return this;
		}

		public ResourceCreditViewModelBuilder endTime(LocalDateTime endTime) {
			this.endTime = endTime;
			return this;
		}

		public ResourceCreditViewModel build() {
			return new ResourceCreditViewModel(id, siteId, resourceTypeId, name, split, access, amount, createTime, startTime, endTime);
		}
	}
}
