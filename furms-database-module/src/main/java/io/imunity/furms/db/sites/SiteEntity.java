/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.db.sites;

import io.imunity.furms.domain.sites.Site;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.util.Objects;
import java.util.UUID;

@Table("SITE")
class SiteEntity {

	@Id
	private final UUID id;

	private final String name;

	SiteEntity(UUID id, String name) {
		this.id = id;
		this.name = name;
	}

	public Site toSite() {
		return Site.builder()
				.id(id.toString())
				.name(name)
				.build();
	}

	public UUID getId() {
		return id;
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
				Objects.equals(name, that.name);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, name);
	}

	@Override
	public String toString() {
		return "SiteEntity{" +
				"id='" + id + '\'' +
				", name='" + name + '\'' +
				'}';
	}

	public static class SiteEntityBuilder {

		private UUID id;
		private String name;

		public SiteEntity.SiteEntityBuilder id(UUID id) {
			this.id = id;
			return this;
		}

		public SiteEntity.SiteEntityBuilder name(String name) {
			this.name = name;
			return this;
		}

		public SiteEntity build() {
			return new SiteEntity(id, name);
		}
	}
}
