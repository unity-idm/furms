/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.db.communities;

import io.imunity.furms.db.id.uuid.UUIDIdentifiable;
import io.imunity.furms.domain.communities.Community;
import org.springframework.data.relational.core.mapping.Table;

import java.util.Arrays;
import java.util.Objects;
import java.util.UUID;

@Table("COMMUNITY")
class CommunityEntity extends UUIDIdentifiable {

	private final String name;
	private final String description;
	private final byte[] logoImage;
	private final String logoType;

	CommunityEntity(UUID id, String name, String description, byte[] logoImage, String logoType) {
		this.id = id;
		this.name = name;
		this.description = description;
		this.logoImage = logoImage;
		this.logoType = logoType;
	}

	public Community toCommunity() {
		return Community.builder()
				.id(id.toString())
				.name(name)
				.description(description)
				.logo(logoImage, logoType)
				.build();
	}

	public String getName() {
		return name;
	}

	public String getDescription() {
		return description;
	}

	public byte[] getLogoImage() {
		return logoImage;
	}

	public String getLogoType() {
		return logoType;
	}

	public static CommunityEntityBuilder builder() {
		return new CommunityEntityBuilder();
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		CommunityEntity that = (CommunityEntity) o;
		return Objects.equals(name, that.name) &&
			Objects.equals(description, that.description) &&
			Arrays.equals(logoImage, that.logoImage) &&
			Objects.equals(logoType, that.logoType);
	}

	@Override
	public int hashCode() {
		int result = Objects.hash(name, description, logoType);
		result = 31 * result + Arrays.hashCode(logoImage);
		return result;
	}

	@Override
	public String toString() {
		return "CommunityEntity{" +
			"id='" + id + '\'' +
			"name='" + name + '\'' +
			", description='" + description + '\'' +
			", logoImage=" + Arrays.toString(logoImage) +
			", logoType='" + logoType + '\'' +
			'}';
	}

	public static class CommunityEntityBuilder {

		private UUID id;
		private String name;
		private String description;
		private byte[] logoImage;
		private String logoType;

		public CommunityEntityBuilder id(UUID id) {
			this.id = id;
			return this;
		}

		public CommunityEntityBuilder name(String name) {
			this.name = name;
			return this;
		}

		public CommunityEntityBuilder description(String description) {
			this.description = description;
			return this;
		}

		public CommunityEntityBuilder logo(byte[] logoImage, String logoType) {
			this.logoImage = logoImage;
			this.logoType = logoType;
			return this;
		}

		public CommunityEntity build() {
			return new CommunityEntity(id, name, description, logoImage, logoType);
		}
	}
}
