/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.domain.sites;

import io.imunity.furms.domain.images.FurmsImage;

import java.util.Objects;

public class Site {

	private final String id;
	private final String name;
	private final String connectionInfo;
	private final FurmsImage logo;

	private Site(String id, String name, String connectionInfo, FurmsImage logo) {
		this.id = id;
		this.name = name;
		this.connectionInfo = connectionInfo;
		this.logo = logo;
	}

	public String getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public String getConnectionInfo() {
		return connectionInfo;
	}

	public FurmsImage getLogo() {
		return logo;
	}

	public static SiteBuilder builder() {
		return new SiteBuilder();
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		Site site = (Site) o;
		return Objects.equals(id, site.id) &&
				Objects.equals(name, site.name) &&
				Objects.equals(connectionInfo, site.connectionInfo) &&
				Objects.equals(logo, site.logo);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, name, connectionInfo, logo);
	}

	@Override
	public String toString() {
		return "Site{" +
				"id=" + id +
				", name='" + name + '\'' +
				", connectionInfo='" + connectionInfo + '\'' +
				", logo=" + logo +
				'}';
	}

	public static class SiteBuilder {

		private String id;
		private String name;
		private String connectionInfo;
		private FurmsImage logo;

		public SiteBuilder id(String id) {
			this.id = id;
			return this;
		}

		public SiteBuilder name(String name) {
			this.name = name;
			return this;
		}

		public SiteBuilder connectionInfo(String connectionInfo) {
			this.connectionInfo = connectionInfo;
			return this;
		}

		public SiteBuilder logo(FurmsImage logo) {
			this.logo = logo;
			return this;
		}

		public Site build() {
			return new Site(id, name, connectionInfo, logo);
		}

	}

}
