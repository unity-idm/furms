/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.views.site.resource_types;

import io.imunity.furms.domain.resource_types.Type;
import io.imunity.furms.domain.resource_types.Unit;

import java.util.Objects;

class ResourceTypeViewModel {
	public final String id;
	public final String siteId;
	public String serviceId;
	public String name;
	public Type type;
	public Unit unit;

	private ResourceTypeViewModel(String id, String siteId, String serviceId, String name, Type type, Unit unit) {
		this.id = id;
		this.siteId = siteId;
		this.serviceId = serviceId;
		this.name = name;
		this.type = type;
		this.unit = unit;
	}

	public ResourceTypeViewModel(String siteId) {
		this.id = null;
		this.serviceId = null;
		this.siteId = siteId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Type getType() {
		return type;
	}

	public void setType(Type type) {
		this.type = type;
	}

	public Unit getUnit() {
		return unit;
	}

	public void setUnit(Unit unit) {
		this.unit = unit;
	}

	public String getServiceId() {
		return serviceId;
	}

	public void setServiceId(String serviceId) {
		this.serviceId = serviceId;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		ResourceTypeViewModel that = (ResourceTypeViewModel) o;
		return Objects.equals(id, that.id);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id);
	}

	@Override
	public String toString() {
		return "ResourceTypeViewModel{" +
			"id='" + id + '\'' +
			", siteId='" + siteId + '\'' +
			", serviceId='" + serviceId + '\'' +
			", name='" + name + '\'' +
			", type=" + type +
			", unit=" + unit +
			'}';
	}

	public static ResourceTypeViewModelBuilder builder() {
		return new ResourceTypeViewModelBuilder();
	}

	public static final class ResourceTypeViewModelBuilder {
		public String id;
		public String siteId;
		public String serviceId;
		public String name;
		public Type type;
		public Unit unit;

		private ResourceTypeViewModelBuilder() {
		}

		public ResourceTypeViewModelBuilder id(String id) {
			this.id = id;
			return this;
		}

		public ResourceTypeViewModelBuilder siteId(String siteId) {
			this.siteId = siteId;
			return this;
		}

		public ResourceTypeViewModelBuilder serviceId(String serviceId) {
			this.serviceId = serviceId;
			return this;
		}

		public ResourceTypeViewModelBuilder name(String name) {
			this.name = name;
			return this;
		}

		public ResourceTypeViewModelBuilder type(Type type) {
			this.type = type;
			return this;
		}

		public ResourceTypeViewModelBuilder unit(Unit unit) {
			this.unit = unit;
			return this;
		}

		public ResourceTypeViewModel build() {
			return new ResourceTypeViewModel(id, siteId, serviceId, name, type, unit);
		}
	}
}
