/*
 * Copyright (c) 2021 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.views.fenix.dashboard;

import java.time.LocalDate;
import java.util.Objects;

public class DashboardGridItem {

	private final String id;
	private final String siteId;
	private final String siteName;
	private final String name;
	private final boolean split;
	private final String resourceTypeId;
	private final DashboardGridResource credit;
	private final DashboardGridResource remaining;
	private final LocalDate created;
	private final LocalDate validFrom;
	private final LocalDate validTo;

	public DashboardGridItem(String id,
	                         String siteId,
	                         String siteName,
	                         String name,
	                         boolean split,
	                         String resourceTypeId,
	                         DashboardGridResource credit,
	                         DashboardGridResource remaining,
	                         LocalDate created,
	                         LocalDate validFrom,
	                         LocalDate validTo) {
		this.id = id;
		this.siteId = siteId;
		this.siteName = siteName;
		this.name = name;
		this.split = split;
		this.resourceTypeId = resourceTypeId;
		this.credit = credit;
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

	public boolean isSplit() {
		return split;
	}

	public String getResourceTypeId() {
		return resourceTypeId;
	}

	public DashboardGridResource getCredit() {
		return credit;
	}

	public DashboardGridResource getRemaining() {
		return remaining;
	}

	public LocalDate getCreated() {
		return created;
	}

	public LocalDate getValidFrom() {
		return validFrom;
	}

	public LocalDate getValidTo() {
		return validTo;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		DashboardGridItem that = (DashboardGridItem) o;
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
				", split='" + split + '\'' +
				", resourceTypeId='" + resourceTypeId + '\'' +
				", credit=" + credit +
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
		private boolean split;
		private String resourceTypeId;
		private DashboardGridResource credit;
		private DashboardGridResource remaining;
		private LocalDate created;
		private LocalDate validFrom;
		private LocalDate validTo;

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

		public DashboardGridItemBuilder remaining(DashboardGridResource remaining) {
			this.remaining = remaining;
			return this;
		}

		public DashboardGridItemBuilder created(LocalDate created) {
			this.created = created;
			return this;
		}

		public DashboardGridItemBuilder validFrom(LocalDate validFrom) {
			this.validFrom = validFrom;
			return this;
		}

		public DashboardGridItemBuilder validTo(LocalDate validTo) {
			this.validTo = validTo;
			return this;
		}

		public DashboardGridItem build() {
			return new DashboardGridItem(id, siteId, siteName, name, split, resourceTypeId, credit,
					remaining, created, validFrom, validTo);
		}
	}
}
