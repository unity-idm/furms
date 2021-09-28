/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.db.generic_groups;

import io.imunity.furms.db.id.uuid.UUIDIdentifiable;
import org.springframework.data.relational.core.mapping.Table;

import java.util.Objects;
import java.util.UUID;

@Table("generic_group")
class GenericGroupEntity extends UUIDIdentifiable {
	public final UUID communityId;
	public final String name;
	public final String description;

	GenericGroupEntity(UUID id, UUID communityId, String name, String description) {
		this.id = id;
		this.communityId = communityId;
		this.name = name;
		this.description = description;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		GenericGroupEntity that = (GenericGroupEntity) o;
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
			"communityId=" + communityId +
			", name='" + name + '\'' +
			", description='" + description + '\'' +
			", id=" + id +
			'}';
	}

	public static GenericGroupEntityBuilder builder() {
		return new GenericGroupEntityBuilder();
	}

	public static final class GenericGroupEntityBuilder {
		private UUID id;
		private UUID communityId;
		private String name;
		private String description;

		private GenericGroupEntityBuilder() {
		}

		public GenericGroupEntityBuilder communityId(UUID communityId) {
			this.communityId = communityId;
			return this;
		}

		public GenericGroupEntityBuilder name(String name) {
			this.name = name;
			return this;
		}

		public GenericGroupEntityBuilder description(String description) {
			this.description = description;
			return this;
		}

		public GenericGroupEntityBuilder id(UUID id) {
			this.id = id;
			return this;
		}

		public GenericGroupEntity build() {
			return new GenericGroupEntity(id, communityId, name, description);
		}
	}
}
