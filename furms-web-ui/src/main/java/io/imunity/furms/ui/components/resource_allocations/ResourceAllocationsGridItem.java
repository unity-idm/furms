/*
 * Copyright (c) 2021 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.components.resource_allocations;

import io.imunity.furms.domain.communities.CommunityId;
import io.imunity.furms.domain.community_allocation.CommunityAllocationId;
import io.imunity.furms.domain.resource_types.ResourceType;
import io.imunity.furms.domain.sites.SiteId;
import io.imunity.furms.ui.views.fenix.dashboard.DashboardGridResource;

import java.time.ZonedDateTime;
import java.util.Objects;

public class ResourceAllocationsGridItem {

	private final CommunityAllocationId id;
	private final SiteId siteId;
	private final String siteName;
	private final String name;
	private final CommunityId communityId;
	private final boolean split;
	private final ResourceType resourceType;
	private final DashboardGridResource credit;
	private final DashboardGridResource distributed;
	private final DashboardGridResource remaining;
	private final ZonedDateTime created;
	private final ZonedDateTime validFrom;
	private final ZonedDateTime validTo;

	ResourceAllocationsGridItem(CommunityAllocationId id,
	                            SiteId siteId,
	                            String siteName,
	                            String name,
	                            CommunityId communityId,
	                            boolean split,
	                            ResourceType resourceType,
	                            DashboardGridResource credit,
	                            DashboardGridResource distributed,
	                            DashboardGridResource remaining,
	                            ZonedDateTime created,
	                            ZonedDateTime validFrom,
	                            ZonedDateTime validTo) {
		this.id = id;
		this.siteId = siteId;
		this.siteName = siteName;
		this.name = name;
		this.communityId = communityId;
		this.split = split;
		this.resourceType = resourceType;
		this.credit = credit;
		this.distributed = distributed;
		this.remaining = remaining;
		this.created = created;
		this.validFrom = validFrom;
		this.validTo = validTo;
	}

	public CommunityAllocationId getId() {
		return id;
	}

	public SiteId getSiteId() {
		return siteId;
	}

	public String getSiteName() {
		return siteName;
	}

	public String getName() {
		return name;
	}

	public CommunityId getCommunityId() {
		return communityId;
	}

	public boolean isSplit() {
		return split;
	}

	public ResourceType getResourceType() {
		return resourceType;
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

	public ZonedDateTime getCreated() {
		return created;
	}

	public ZonedDateTime getValidFrom() {
		return validFrom;
	}

	public ZonedDateTime getValidTo() {
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
				", resourceType='" + resourceType + '\'' +
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
		private CommunityAllocationId id;
		private SiteId siteId;
		private String siteName;
		private String name;
		private CommunityId communityId;
		private boolean split;
		private ResourceType resourceType;
		private DashboardGridResource credit;
		private DashboardGridResource distributed;
		private DashboardGridResource remaining;
		private ZonedDateTime created;
		private ZonedDateTime validFrom;
		private ZonedDateTime validTo;

		private DashboardGridItemBuilder() {
		}

		public DashboardGridItemBuilder id(CommunityAllocationId id) {
			this.id = id;
			return this;
		}

		public DashboardGridItemBuilder siteId(SiteId siteId) {
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

		public DashboardGridItemBuilder communityId(CommunityId communityId) {
			this.communityId = communityId;
			return this;
		}

		public DashboardGridItemBuilder split(boolean split) {
			this.split = split;
			return this;
		}

		public DashboardGridItemBuilder resourceType(ResourceType resourceType) {
			this.resourceType = resourceType;
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

		public DashboardGridItemBuilder created(ZonedDateTime created) {
			this.created = created;
			return this;
		}

		public DashboardGridItemBuilder validFrom(ZonedDateTime validFrom) {
			this.validFrom = validFrom;
			return this;
		}

		public DashboardGridItemBuilder validTo(ZonedDateTime validTo) {
			this.validTo = validTo;
			return this;
		}

		public ResourceAllocationsGridItem build() {
			return new ResourceAllocationsGridItem(id, siteId, siteName, name, communityId, split, resourceType, credit,
					distributed, remaining, created, validFrom, validTo);
		}
	}
}
