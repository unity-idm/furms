/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.db.resource_types;

import io.imunity.furms.db.id.uuid.UUIDIdentifiable;
import io.imunity.furms.domain.resource_types.ResourceType;
import io.imunity.furms.domain.resource_types.ResourceMeasureType;
import io.imunity.furms.domain.resource_types.ResourceMeasureUnit;
import org.springframework.data.relational.core.mapping.Table;

import java.util.Objects;
import java.util.UUID;

@Table("resource_type")
public class ResourceTypeEntity extends UUIDIdentifiable {

	public final UUID siteId;
	public final UUID serviceId;
	public final String name;
	public final ResourceMeasureType type;
	public final ResourceMeasureUnit unit;
	public final boolean accessible;

	public ResourceTypeEntity(UUID id, String name, UUID siteId, UUID serviceId, ResourceMeasureType type,
	                          ResourceMeasureUnit unit, boolean accessible) {
		this.id = id;
		this.name = name;
		this.siteId = siteId;
		this.serviceId = serviceId;
		this.type = type;
		this.unit = unit;
		this.accessible = accessible;
	}

	public ResourceType toResourceType() {
		return ResourceType.builder()
			.id(id.toString())
			.siteId(siteId.toString())
			.serviceId(serviceId.toString())
			.name(name)
			.type(type)
			.unit(unit)
			.accessibleForAllProjectMembers(accessible)
			.build();
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		ResourceTypeEntity that = (ResourceTypeEntity) o;
		return Objects.equals(name, that.name) &&
			Objects.equals(siteId, that.siteId) &&
			Objects.equals(serviceId, that.serviceId) &&
			type == that.type &&
			Objects.equals(unit, that.unit) &&
			Objects.equals(accessible, that.accessible);
	}

	@Override
	public int hashCode() {
		return Objects.hash(name, siteId, serviceId, type, unit, accessible);
	}

	@Override
	public String toString() {
		return "ResourceTypeEntity{" +
			"name='" + name + '\'' +
			", siteId=" + siteId +
			", serviceId=" + serviceId +
			", type=" + type +
			", unit=" + unit +
			", id=" + id +
			", accessible=" + accessible +
			'}';
	}

	public static ResourceTypeEntityBuilder builder() {
		return new ResourceTypeEntityBuilder();
	}

	public static final class ResourceTypeEntityBuilder {
		public UUID siteId;
		public UUID serviceId;
		public ResourceMeasureType type;
		public ResourceMeasureUnit unit;
		protected UUID id;
		private String name;
		private boolean accessible;

		private ResourceTypeEntityBuilder() {
		}

		public ResourceTypeEntityBuilder name(String name) {
			this.name = name;
			return this;
		}

		public ResourceTypeEntityBuilder siteId(UUID siteId) {
			this.siteId = siteId;
			return this;
		}

		public ResourceTypeEntityBuilder serviceId(UUID serviceId) {
			this.serviceId = serviceId;
			return this;
		}

		public ResourceTypeEntityBuilder type(ResourceMeasureType type) {
			this.type = type;
			return this;
		}

		public ResourceTypeEntityBuilder unit(ResourceMeasureUnit unit) {
			this.unit = unit;
			return this;
		}

		public ResourceTypeEntityBuilder accessible(boolean accessible) {
			this.accessible = accessible;
			return this;
		}

		public ResourceTypeEntityBuilder id(UUID id) {
			this.id = id;
			return this;
		}

		public ResourceTypeEntity build() {
			return new ResourceTypeEntity(id, name, siteId, serviceId, type, unit, accessible);
		}
	}
}
