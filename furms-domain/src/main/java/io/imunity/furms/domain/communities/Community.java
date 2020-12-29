/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.domain.communities;

import java.util.Arrays;
import java.util.Objects;

public class Community {
	private final String id;
	private final String name;
	private final String description;
	private final byte[] logoImage;

	private Community(String id, String name, String description, byte[] logoImage) {
		this.id = id;
		this.name = name;
		this.description = description;
		this.logoImage = logoImage;
	}

	public String getId() {
		return id;
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
			Arrays.equals(logoImage, community.logoImage);
	}

	@Override
	public int hashCode() {
		int result = Objects.hash(id, name, description);
		result = 31 * result + Arrays.hashCode(logoImage);
		return result;
	}

	@Override
	public String toString() {
		return "Community{" +
			"id='" + id + '\'' +
			", name='" + name + '\'' +
			", description='" + description + '\'' +
			", logoImage=" + Arrays.toString(logoImage) +
			'}';
	}

	public static class CommunityBuilder {
		private String id;
		private String name;
		private String description;
		private byte[] logoImage;

		public CommunityBuilder id(String id) {
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

		public CommunityBuilder logoImage(byte[] logoImage) {
			this.logoImage = logoImage;
			return this;
		}

		public Community build() {
			return new Community(id, name, description, logoImage);
		}
	}
}
