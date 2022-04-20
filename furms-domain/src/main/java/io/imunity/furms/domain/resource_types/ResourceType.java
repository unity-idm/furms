/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.domain.resource_types;

import io.imunity.furms.domain.services.InfraServiceId;
import io.imunity.furms.domain.sites.SiteId;

import java.util.Objects;

public class ResourceType {
	public final ResourceTypeId id;
	public final String name;
	public final SiteId siteId;
	public final InfraServiceId serviceId;
	public final String serviceName;
	public final ResourceMeasureType type;
	public final ResourceMeasureUnit unit;
	public final boolean accessibleForAllProjectMembers;

	public ResourceType(ResourceTypeId id, String name, SiteId siteId, InfraServiceId serviceId, String serviceName,
	                    ResourceMeasureType type, ResourceMeasureUnit unit, boolean accessibleForAllProjectMembers) {
		this.id = id;
		this.name = name;
		this.siteId = siteId;
		this.serviceId = serviceId;
		this.serviceName = serviceName;
		this.type = type;
		this.unit = unit;
		this.accessibleForAllProjectMembers = accessibleForAllProjectMembers;
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
			Objects.equals(serviceName, that.serviceName) &&
			type == that.type &&
			Objects.equals(unit, that.unit) &&
			Objects.equals(accessibleForAllProjectMembers, that.accessibleForAllProjectMembers);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, name, siteId, serviceId, serviceName, type, unit, accessibleForAllProjectMembers);
	}

	@Override
	public String toString() {
		return "ResourceType{" +
			"id='" + id + '\'' +
			", name='" + name + '\'' +
			", siteId='" + siteId + '\'' +
			", serviceId='" + serviceId + '\'' +
			", serviceName='" + serviceName + '\'' +
			", type=" + type +
			", unit=" + unit +
			", accessible=" + accessibleForAllProjectMembers +
			'}';
	}

	public static ResourceTypeBuilder builder() {
		return new ResourceTypeBuilder();
	}

	public static final class ResourceTypeBuilder {
		public ResourceTypeId id;
		public String name;
		public SiteId siteId;
		public InfraServiceId serviceId;
		public String serviceName;
		public ResourceMeasureType type;
		public ResourceMeasureUnit unit;
		public boolean accessibleForAllProjectMembers;

		private ResourceTypeBuilder() {
		}

		public ResourceTypeBuilder id(String id) {
			this.id = new ResourceTypeId(id);
			return this;
		}

		public ResourceTypeBuilder name(String name) {
			this.name = name;
			return this;
		}

		public ResourceTypeBuilder siteId(String siteId) {
			this.siteId = new SiteId(siteId);
			return this;
		}

		public ResourceTypeBuilder serviceId(String serviceId) {
			this.serviceId = new InfraServiceId(serviceId);
			return this;
		}

		public void serviceName(String serviceName) {
			this.serviceName = serviceName;
		}

		public ResourceTypeBuilder type(ResourceMeasureType type) {
			this.type = type;
			return this;
		}

		public ResourceTypeBuilder unit(ResourceMeasureUnit unit) {
			this.unit = unit;
			return this;
		}

		public ResourceTypeBuilder accessibleForAllProjectMembers(boolean accessible) {
			this.accessibleForAllProjectMembers = accessible;
			return this;
		}

		public ResourceType build() {
			return new ResourceType(id, name, siteId, serviceId, serviceName, type, unit, accessibleForAllProjectMembers);
		}
	}
}
