/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.domain.sites;

import java.util.Objects;

public class Site {

	private final String id;
	private final String name;

	private Site(String id, String name) {
		this.id = id;
		this.name = name;
	}

	public String getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public static SiteBuilder builder() {
		return new SiteBuilder();
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		final Site site = (Site) o;
		return Objects.equals(id, site.id) &&
				Objects.equals(name, site.name);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, name);
	}

	@Override
	public String toString() {
		return "Site{" +
				"id=" + id +
				", name='" + name + '\'' +
				'}';
	}

	public static class SiteBuilder {

		private String id;
		private String name;

		public SiteBuilder id(String id) {
			this.id = id;
			return this;
		}

		public SiteBuilder name(String name) {
			this.name = name;
			return this;
		}

		public Site build() {
			return new Site(id, name);
		}

	}

}
