/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.db.sites;

import java.util.Arrays;
import java.util.Objects;
import java.util.UUID;

import org.springframework.data.relational.core.mapping.Table;

import io.imunity.furms.db.id.uuid.UUIDIdentifiable;
import io.imunity.furms.domain.images.FurmsImage;
import io.imunity.furms.domain.sites.Site;

@Table("site")
class SiteEntity extends UUIDIdentifiable {

	private final String name;
	private final String connectionInfo;
	private final byte[] logo;
	private final String logoType;

	SiteEntity(UUID id, String name, String connectionInfo, byte[] logo, String logoType) {
		this.id = id;
		this.name = name;
		this.connectionInfo = connectionInfo;
		this.logo = logo;
		this.logoType = logoType;
	}

	public Site toSite() {
		return Site.builder()
				.id(id.toString())
				.name(name)
				.connectionInfo(connectionInfo)
				.logo(new FurmsImage(logo, logoType))
				.build();
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
		SiteEntity entity = (SiteEntity) o;
		return Objects.equals(id, entity.id) &&
				Objects.equals(name, entity.name) &&
				Objects.equals(connectionInfo, entity.connectionInfo) &&
				Arrays.equals(logo, entity.logo) &&
				Objects.equals(logoType, entity.logoType);
	}

	@Override
	public int hashCode() {
		int result = Objects.hash(id, name, connectionInfo, logoType);
		result = 31 * result + Arrays.hashCode(logo);
		return result;
	}

	@Override
	public String toString() {
		return "SiteEntity{" +
				"id='" + id + '\'' +
				", name='" + name + '\'' +
				", connectionInfo='" + connectionInfo + '\'' +
				", logo=" + Arrays.toString(logo) +
				", logoType='" + logoType + '\'' +
				'}';
	}

	public static class SiteEntityBuilder {

		private UUID id;
		private String name;
		private String connectionInfo;
		private byte[] logo;
		private String logoType;

		public SiteEntity.SiteEntityBuilder id(UUID id) {
			this.id = id;
			return this;
		}

		public SiteEntity.SiteEntityBuilder name(String name) {
			this.name = name;
			return this;
		}

		public SiteEntity.SiteEntityBuilder connectionInfo(String connectionInfo) {
			this.connectionInfo = connectionInfo;
			return this;
		}

		public SiteEntity.SiteEntityBuilder logo(FurmsImage logo) {
			if (logo != null) {
				this.logo = logo.getImage();
				this.logoType = logo.getType();
			}
			return this;
		}

		public SiteEntity build() {
			return new SiteEntity(id, name, connectionInfo, logo, logoType);
		}
	}
}
