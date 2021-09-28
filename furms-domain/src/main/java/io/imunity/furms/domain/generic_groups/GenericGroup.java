/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.domain.generic_groups;

import java.util.Objects;
import java.util.UUID;

public class GenericGroup {
	public final GenericGroupId id;
	public final String communityId;
	public final String name;
	public final String description;

	GenericGroup(GenericGroupId id, String communityId, String name, String description) {
		this.id = id;
		this.communityId = communityId;
		this.name = name;
		this.description = description;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		GenericGroup that = (GenericGroup) o;
		return Objects.equals(id, that.id) &&
			Objects.equals(communityId, that.communityId) &&
			Objects.equals(name, that.name) &&
			Objects.equals(description, that.description);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, communityId, name, description);
	}

	@Override
	public String toString() {
		return "GenericGroup{" +
			"id=" + id +
			", communityId='" + communityId + '\'' +
			", name='" + name + '\'' +
			", description='" + description + '\'' +
			'}';
	}

	public static GenericGroupBuilder builder() {
		return new GenericGroupBuilder();
	}

	public static final class GenericGroupBuilder {
		private GenericGroupId id;
		private String communityId;
		private String name;
		private String description;

		private GenericGroupBuilder() {
		}

		public GenericGroupBuilder id(GenericGroupId id) {
			this.id = id;
			return this;
		}

		public GenericGroupBuilder id(UUID id) {
			this.id = new GenericGroupId(id);
			return this;
		}

		public GenericGroupBuilder communityId(String communityId) {
			this.communityId = communityId;
			return this;
		}

		public GenericGroupBuilder name(String name) {
			this.name = name;
			return this;
		}

		public GenericGroupBuilder description(String description) {
			this.description = description;
			return this;
		}

		public GenericGroup build() {
			return new GenericGroup(id, communityId, name, description);
		}
	}
}
