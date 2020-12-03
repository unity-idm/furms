/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.db.sites;

import io.imunity.furms.domain.sites.Site;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.util.Objects;

@Table("SITE")
class SiteEntity {

	@Id
	private final Long id;

	private final String siteId;

	private final String name;

	SiteEntity(Long id, String siteId, String name) {
		this.id = id;
		this.siteId = siteId;
		this.name = name;
	}

	public Site toSite() {
		return Site.builder()
				.id(siteId)
				.name(name)
				.build();
	}

	public Long getId() {
		return id;
	}

	public String getSiteId() {
		return siteId;
	}

	public String getName() {
		return name;
	}

	public static SiteEntity.SiteEntityBuilder builder() {
		return new SiteEntity.SiteEntityBuilder();
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		SiteEntity that = (SiteEntity) o;
		return Objects.equals(id, that.id) &&
				Objects.equals(siteId, that.siteId) &&
				Objects.equals(name, that.name);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, siteId, name);
	}

	@Override
	public String toString() {
		return "SiteEntity{" +
				"id=" + id +
				", siteId='" + siteId + '\'' +
				", name='" + name + '\'' +
				'}';
	}

	public static class SiteEntityBuilder {

		private Long id;
		private String siteId;
		private String name;

		public SiteEntity.SiteEntityBuilder id(Long id) {
			this.id = id;
			return this;
		}

		public SiteEntity.SiteEntityBuilder siteId(String siteId) {
			this.siteId = siteId;
			return this;
		}

		public SiteEntity.SiteEntityBuilder name(String name) {
			this.name = name;
			return this;
		}

		public SiteEntity build() {
			return new SiteEntity(id, siteId, name);
		}

	}
}
