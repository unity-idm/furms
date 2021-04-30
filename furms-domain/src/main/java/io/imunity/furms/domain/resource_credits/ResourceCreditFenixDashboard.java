/*
 * Copyright (c) 2021 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.domain.resource_credits;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

public class ResourceCreditFenixDashboard {
	public final String id;
	public final String name;
	public final String siteId;
	public final String resourceTypeId;
	public final Boolean split;
	public final Boolean access;
	public final BigDecimal amount;
	public final BigDecimal remaining;
	public final LocalDateTime utcCreateTime;
	public final LocalDateTime utcStartTime;
	public final LocalDateTime utcEndTime;

	public ResourceCreditFenixDashboard(String id,
	                                    String name,
	                                    String siteId,
	                                    String resourceTypeId,
	                                    Boolean split,
	                                    Boolean access,
	                                    BigDecimal amount,
	                                    BigDecimal remaining,
	                                    LocalDateTime utcCreateTime,
	                                    LocalDateTime utcStartTime,
	                                    LocalDateTime utcEndTime) {
		this.id = id;
		this.name = name;
		this.siteId = siteId;
		this.resourceTypeId = resourceTypeId;
		this.split = split;
		this.access = access;
		this.amount = amount;
		this.remaining = remaining;
		this.utcCreateTime = utcCreateTime;
		this.utcStartTime = utcStartTime;
		this.utcEndTime = utcEndTime;
	}

	public String getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public String getSiteId() {
		return siteId;
	}

	public String getResourceTypeId() {
		return resourceTypeId;
	}

	public Boolean getSplit() {
		return split;
	}

	public Boolean getAccess() {
		return access;
	}

	public BigDecimal getAmount() {
		return amount;
	}

	public BigDecimal getRemaining() {
		return remaining;
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
		ResourceCreditFenixDashboard that = (ResourceCreditFenixDashboard) o;
		return Objects.equals(id, that.id) &&
				Objects.equals(name, that.name) &&
				Objects.equals(siteId, that.siteId) &&
				Objects.equals(resourceTypeId, that.resourceTypeId) &&
				Objects.equals(split, that.split) &&
				Objects.equals(access, that.access) &&
				Objects.equals(amount, that.amount) &&
				Objects.equals(remaining, that.remaining) &&
				Objects.equals(utcCreateTime, that.utcCreateTime) &&
				Objects.equals(utcStartTime, that.utcStartTime) &&
				Objects.equals(utcEndTime, that.utcEndTime);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, name, siteId, resourceTypeId, split, access, amount, remaining, utcCreateTime, utcStartTime, utcEndTime);
	}

	@Override
	public String toString() {
		return "ResourceCreditFenixDashboard{" +
				"id='" + id + '\'' +
				", name='" + name + '\'' +
				", siteId='" + siteId + '\'' +
				", resourceTypeId='" + resourceTypeId + '\'' +
				", split=" + split +
				", access=" + access +
				", amount=" + amount +
				", remaining=" + remaining +
				", utcCreateTime=" + utcCreateTime +
				", utcStartTime=" + utcStartTime +
				", utcEndTime=" + utcEndTime +
				'}';
	}

	public static ResourceCreditFenixDashboardBuilder builder() {
		return new ResourceCreditFenixDashboardBuilder();
	}

	public static final class ResourceCreditFenixDashboardBuilder {
		public String id;
		public String name;
		public String siteId;
		public String resourceTypeId;
		public Boolean split;
		public Boolean access;
		public BigDecimal amount;
		public BigDecimal remaining;
		public LocalDateTime utcCreateTime;
		public LocalDateTime utcStartTime;
		public LocalDateTime utcEndTime;

		private ResourceCreditFenixDashboardBuilder() {
		}

		public ResourceCreditFenixDashboardBuilder id(String id) {
			this.id = id;
			return this;
		}

		public ResourceCreditFenixDashboardBuilder name(String name) {
			this.name = name;
			return this;
		}

		public ResourceCreditFenixDashboardBuilder siteId(String siteId) {
			this.siteId = siteId;
			return this;
		}

		public ResourceCreditFenixDashboardBuilder resourceTypeId(String resourceTypeId) {
			this.resourceTypeId = resourceTypeId;
			return this;
		}

		public ResourceCreditFenixDashboardBuilder split(Boolean split) {
			this.split = split;
			return this;
		}

		public ResourceCreditFenixDashboardBuilder access(Boolean access) {
			this.access = access;
			return this;
		}

		public ResourceCreditFenixDashboardBuilder amount(BigDecimal amount) {
			this.amount = amount;
			return this;
		}

		public ResourceCreditFenixDashboardBuilder remaining(BigDecimal remaining) {
			this.remaining = remaining;
			return this;
		}

		public ResourceCreditFenixDashboardBuilder utcCreateTime(LocalDateTime utcCreateTime) {
			this.utcCreateTime = utcCreateTime;
			return this;
		}

		public ResourceCreditFenixDashboardBuilder utcStartTime(LocalDateTime utcStartTime) {
			this.utcStartTime = utcStartTime;
			return this;
		}

		public ResourceCreditFenixDashboardBuilder utcEndTime(LocalDateTime utcEndTime) {
			this.utcEndTime = utcEndTime;
			return this;
		}

		public ResourceCreditFenixDashboard build() {
			return new ResourceCreditFenixDashboard(id, name, siteId, resourceTypeId, split, access, amount, remaining, utcCreateTime, utcStartTime, utcEndTime);
		}
	}
}
