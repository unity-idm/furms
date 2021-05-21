/*
 * Copyright (c) 2021 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.components.resource_allocations;

import io.imunity.furms.ui.views.fenix.dashboard.DashboardGridResource;

import java.time.LocalDateTime;
import java.util.Objects;

public class ResourceAllocationsGridItem {

	private final String id;
	private final String siteId;
	private final String siteName;
	private final String name;
	private final String communityId;
	private final boolean split;
	private final String resourceTypeId;
	private final DashboardGridResource credit;
	private final DashboardGridResource distributed;
	private final DashboardGridResource remaining;
	private final LocalDateTime created;
	private final LocalDateTime validFrom;
	private final LocalDateTime validTo;

	ResourceAllocationsGridItem(String id,
	                            String siteId,
	                            String siteName,
	                            String name,
	                            String communityId,
	                            boolean split,
	                            String resourceTypeId,
	                            DashboardGridResource credit,
	                            DashboardGridResource distributed,
	                            DashboardGridResource remaining,
	                            LocalDateTime created,
	                            LocalDateTime validFrom,
	                            LocalDateTime validTo) {
		this.id = id;
		this.siteId = siteId;
		this.siteName = siteName;
		this.name = name;
		this.communityId = communityId;
		this.split = split;
		this.resourceTypeId = resourceTypeId;
		this.credit = credit;
		this.distributed = distributed;
		this.remaining = remaining;
		this.created = created;
		this.validFrom = validFrom;
		this.validTo = validTo;
	}

	public String getId() {
		return id;
	}

	public String getSiteId() {
		return siteId;
	}

	public String getSiteName() {
		return siteName;
	}

	public String getName() {
		return name;
	}

	public String getCommunityId() {
		return communityId;
	}

	public boolean isSplit() {
		return split;
	}

	public String getResourceTypeId() {
		return resourceTypeId;
	}

	public DashboardGridResource getCredit() {
		return credit;
	}

	public DashboardGridResource getDistributed() {
		return distributed;
	}

	public DashboardGridResource getRemaining() {
		return remaining;
	}

	public LocalDateTime getCreated() {
		return created;
	}

	public LocalDateTime getValidFrom() {
		return validFrom;
	}

	public LocalDateTime getValidTo() {
		return validTo;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		ResourceAllocationsGridItem that = (ResourceAllocationsGridItem) o;
		return Objects.equals(id, that.id);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id);
	}

	@Override
	public String toString() {
		return "DashboardGridItem{" +
				"id='" + id + '\'' +
				", siteId='" + siteId + '\'' +
				", siteName='" + siteName + '\'' +
				", name='" + name + '\'' +
				", communityId='" + communityId + '\'' +
				", split='" + split + '\'' +
				", resourceTypeId='" + resourceTypeId + '\'' +
				", credit=" + credit +
				", distributed=" + distributed +
				", remaining=" + remaining +
				", created=" + created +
				", validFrom=" + validFrom +
				", validTo=" + validTo +
				'}';
	}

	public static DashboardGridItemBuilder builder() {
		return new DashboardGridItemBuilder();
	}

	public static final class DashboardGridItemBuilder {
		private String id;
		private String siteId;
		private String siteName;
		private String name;
		private String communityId;
		private boolean split;
		private String resourceTypeId;
		private DashboardGridResource credit;
		private DashboardGridResource distributed;
		private DashboardGridResource remaining;
		private LocalDateTime created;
		private LocalDateTime validFrom;
		private LocalDateTime validTo;

		private DashboardGridItemBuilder() {
		}

		public DashboardGridItemBuilder id(String id) {
			this.id = id;
			return this;
		}

		public DashboardGridItemBuilder siteId(String siteId) {
			this.siteId = siteId;
			return this;
		}

		public DashboardGridItemBuilder siteName(String siteName) {
			this.siteName = siteName;
			return this;
		}

		public DashboardGridItemBuilder name(String name) {
			this.name = name;
			return this;
		}

		public DashboardGridItemBuilder communityId(String communityId) {
			this.communityId = communityId;
			return this;
		}

		public DashboardGridItemBuilder split(boolean split) {
			this.split = split;
			return this;
		}

		public DashboardGridItemBuilder resourceTypeId(String resourceTypeId) {
			this.resourceTypeId = resourceTypeId;
			return this;
		}

		public DashboardGridItemBuilder credit(DashboardGridResource credit) {
			this.credit = credit;
			return this;
		}

		public DashboardGridItemBuilder distributed(DashboardGridResource distributed) {
			this.distributed = distributed;
			return this;
		}

		public DashboardGridItemBuilder remaining(DashboardGridResource remaining) {
			this.remaining = remaining;
			return this;
		}

		public DashboardGridItemBuilder created(LocalDateTime created) {
			this.created = created;
			return this;
		}

		public DashboardGridItemBuilder validFrom(LocalDateTime validFrom) {
			this.validFrom = validFrom;
			return this;
		}

		public DashboardGridItemBuilder validTo(LocalDateTime validTo) {
			this.validTo = validTo;
			return this;
		}

		public ResourceAllocationsGridItem build() {
			return new ResourceAllocationsGridItem(id, siteId, siteName, name, communityId, split, resourceTypeId, credit,
					distributed, remaining, created, validFrom, validTo);
		}
	}
}
