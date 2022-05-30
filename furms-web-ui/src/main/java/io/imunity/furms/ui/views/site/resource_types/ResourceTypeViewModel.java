/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.views.site.resource_types;

import io.imunity.furms.domain.resource_types.ResourceMeasureType;
import io.imunity.furms.domain.resource_types.ResourceMeasureUnit;
import io.imunity.furms.domain.resource_types.ResourceTypeId;
import io.imunity.furms.domain.services.InfraServiceId;
import io.imunity.furms.domain.sites.SiteId;

import java.util.Objects;

class ResourceTypeViewModel {
	private final ResourceTypeId id;
	private final SiteId siteId;
	private InfraServiceId serviceId;
	private String name;
	private ResourceMeasureType type;
	private ResourceMeasureUnit unit;
	private boolean accessible;

	private ResourceTypeViewModel(ResourceTypeId id,
	                              SiteId siteId,
	                              InfraServiceId serviceId,
	                              String name,
	                              ResourceMeasureType type,
	                              ResourceMeasureUnit unit,
	                              boolean accessible) {
		this.id = id;
		this.siteId = siteId;
		this.serviceId = serviceId;
		this.name = name;
		this.type = type;
		this.unit = unit;
		this.accessible = accessible;
	}

	public ResourceTypeViewModel(SiteId siteId) {
		this.id = null;
		this.serviceId = null;
		this.siteId = siteId;
	}

	public ResourceTypeId getId() {
		return id;
	}

	public SiteId getSiteId() {
		return siteId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public ResourceMeasureType getType() {
		return type;
	}

	public void setType(ResourceMeasureType type) {
		this.type = type;
	}

	public ResourceMeasureUnit getUnit() {
		return unit;
	}

	public void setUnit(ResourceMeasureUnit unit) {
		this.unit = unit;
	}

	public InfraServiceId getServiceId() {
		return serviceId;
	}

	public void setServiceId(InfraServiceId serviceId) {
		this.serviceId = serviceId;
	}

	boolean isAccessible() {
		return accessible;
	}

	void setAccessible(boolean accessible) {
		this.accessible = accessible;
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
			", accessible=" + accessible +
			'}';
	}

	public static ResourceTypeViewModelBuilder builder() {
		return new ResourceTypeViewModelBuilder();
	}

	public static final class ResourceTypeViewModelBuilder {
		private ResourceTypeId id;
		private SiteId siteId;
		private InfraServiceId serviceId;
		private String name;
		private ResourceMeasureType type;
		private ResourceMeasureUnit unit;
		private boolean accessible;

		private ResourceTypeViewModelBuilder() {
		}

		public ResourceTypeViewModelBuilder id(ResourceTypeId id) {
			this.id = id;
			return this;
		}

		public ResourceTypeViewModelBuilder siteId(SiteId siteId) {
			this.siteId = siteId;
			return this;
		}

		public ResourceTypeViewModelBuilder serviceId(InfraServiceId serviceId) {
			this.serviceId = serviceId;
			return this;
		}

		public ResourceTypeViewModelBuilder name(String name) {
			this.name = name;
			return this;
		}

		public ResourceTypeViewModelBuilder type(ResourceMeasureType type) {
			this.type = type;
			return this;
		}

		public ResourceTypeViewModelBuilder unit(ResourceMeasureUnit unit) {
			this.unit = unit;
			return this;
		}

		public ResourceTypeViewModelBuilder accessible(boolean accessible) {
			this.accessible = accessible;
			return this;
		}

		public ResourceTypeViewModel build() {
			return new ResourceTypeViewModel(id, siteId, serviceId, name, type, unit, accessible);
		}
	}
}
