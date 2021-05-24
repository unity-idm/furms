/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.domain.resource_types;

import java.util.Objects;

public class ResourceType {
	public final String id;
	public final String name;
	public final String siteId;
	public final String serviceId;
	public final ResourceMeasureType type;
	public final ResourceMeasureUnit unit;

	public ResourceType(String id, String name, String siteId, String serviceId, ResourceMeasureType type,
	                    ResourceMeasureUnit unit) {
		this.id = id;
		this.name = name;
		this.siteId = siteId;
		this.serviceId = serviceId;
		this.type = type;
		this.unit = unit;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		ResourceType that = (ResourceType) o;
		return Objects.equals(id, that.id) &&
			Objects.equals(name, that.name) &&
			Objects.equals(siteId, that.siteId) &&
			Objects.equals(serviceId, that.serviceId) &&
			type == that.type &&
			Objects.equals(unit, that.unit);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, name, siteId, serviceId, type, unit);
	}

	@Override
	public String toString() {
		return "ResourceType{" +
			"id='" + id + '\'' +
			", name='" + name + '\'' +
			", siteId='" + siteId + '\'' +
			", serviceId='" + serviceId + '\'' +
			", type=" + type +
			", unit=" + unit +
			'}';
	}

	public static ResourceTypeBuilder builder() {
		return new ResourceTypeBuilder();
	}

	public static final class ResourceTypeBuilder {
		public String id;
		public String name;
		public String siteId;
		public String serviceId;
		public ResourceMeasureType type;
		public ResourceMeasureUnit unit;

		private ResourceTypeBuilder() {
		}

		public ResourceTypeBuilder id(String id) {
			this.id = id;
			return this;
		}

		public ResourceTypeBuilder name(String name) {
			this.name = name;
			return this;
		}

		public ResourceTypeBuilder siteId(String siteId) {
			this.siteId = siteId;
			return this;
		}

		public ResourceTypeBuilder serviceId(String serviceId) {
			this.serviceId = serviceId;
			return this;
		}

		public ResourceTypeBuilder type(ResourceMeasureType type) {
			this.type = type;
			return this;
		}

		public ResourceTypeBuilder unit(ResourceMeasureUnit unit) {
			this.unit = unit;
			return this;
		}

		public ResourceType build() {
			return new ResourceType(id, name, siteId, serviceId, type, unit);
		}
	}
}
