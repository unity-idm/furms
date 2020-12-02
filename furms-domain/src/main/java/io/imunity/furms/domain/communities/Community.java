/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.domain.communities;

import java.util.Arrays;
import java.util.Objects;

public class Community {
	private final String id;
	private final String userFacingName;
	private final String description;
	private final byte[] logoImage;

	private Community(String id, String userFacingName, String description, byte[] logoImage) {
		this.id = id;
		this.userFacingName = userFacingName;
		this.description = description;
		this.logoImage = logoImage;
	}

	public String getId() {
		return id;
	}

	public String getUserFacingName() {
		return userFacingName;
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
			Objects.equals(userFacingName, community.userFacingName) &&
			Objects.equals(description, community.description) &&
			Arrays.equals(logoImage, community.logoImage);
	}

	@Override
	public int hashCode() {
		int result = Objects.hash(id, userFacingName, description);
		result = 31 * result + Arrays.hashCode(logoImage);
		return result;
	}

	@Override
	public String toString() {
		return "Community{" +
			"id='" + id + '\'' +
			", userFacingName='" + userFacingName + '\'' +
			", description='" + description + '\'' +
			", logoImage=" + Arrays.toString(logoImage) +
			'}';
	}

	public static class CommunityBuilder {
		private String id;
		private String userFacingName;
		private String description;
		private byte[] logoImage;

		public CommunityBuilder id(String id) {
			this.id = id;
			return this;
		}

		public CommunityBuilder userFacingName(String userFacingName) {
			this.userFacingName = userFacingName;
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
			return new Community(id, userFacingName, description, logoImage);
		}
	}
}
