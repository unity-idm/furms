/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.domain.communities;

import io.imunity.furms.domain.images.FurmsImage;

import java.util.Objects;

public class Community {
	private final CommunityId id;
	private final String name;
	private final String description;
	private final FurmsImage logo;

	private Community(CommunityId id, String name, String description, FurmsImage logo) {
		this.id = id;
		this.name = name;
		this.description = description;
		this.logo = logo;
	}

	public CommunityId getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public String getDescription() {
		return description;
	}

	public FurmsImage getLogo() {
		return logo;
	}

	public static Community.CommunityBuilder builder() {
		return new Community.CommunityBuilder();
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		Community community = (Community) o;
		return Objects.equals(id, community.id) &&
			Objects.equals(name, community.name) &&
			Objects.equals(description, community.description) &&
			Objects.equals(logo, community.logo);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, name, description, logo);
	}

	@Override
	public String toString() {
		return "Community{" +
			"id='" + id + '\'' +
			", name='" + name + '\'' +
			", description='" + description + '\'' +
			'}';
	}

	public static class CommunityBuilder {
		private CommunityId id;
		private String name;
		private String description;
		private FurmsImage logo;

		private CommunityBuilder() {
		}

		public CommunityBuilder id(String id) {
			this.id = new CommunityId(id);
			return this;
		}

		public CommunityBuilder id(CommunityId id) {
			this.id = id;
			return this;
		}

		public CommunityBuilder name(String name) {
			this.name = name;
			return this;
		}

		public CommunityBuilder description(String description) {
			this.description = description;
			return this;
		}

		public CommunityBuilder logo(byte[] logoImage, String imageType) {
			this.logo = new FurmsImage(logoImage, imageType);
			return this;
		}

		public CommunityBuilder logo(FurmsImage logo) {
			this.logo = logo;
			return this;
		}

		public Community build() {
			return new Community(id, name, description, logo);
		}
	}
}
