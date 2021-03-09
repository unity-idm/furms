/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.domain.services;

import java.util.Objects;

public class Service {
	public final String id;
	public final String name;
	public final String description;
	public final String siteId;

	public Service(String id, String name, String description, String siteId) {
		this.id = id;
		this.name = name;
		this.description = description;
		this.siteId = siteId;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		Service service = (Service) o;
		return Objects.equals(id, service.id) &&
			Objects.equals(name, service.name) &&
			Objects.equals(description, service.description) &&
			Objects.equals(siteId, service.siteId);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, name, description, siteId);
	}

	@Override
	public String toString() {
		return "Service{" +
			"id='" + id + '\'' +
			", name='" + name + '\'' +
			", description='" + description + '\'' +
			", siteId='" + siteId + '\'' +
			'}';
	}

	public static ServiceBuilder builder() {
		return new ServiceBuilder();
	}

	public static final class ServiceBuilder {
		public String id;
		public String name;
		public String description;
		public String siteId;

		private ServiceBuilder() {
		}

		public ServiceBuilder id(String id) {
			this.id = id;
			return this;
		}

		public ServiceBuilder name(String name) {
			this.name = name;
			return this;
		}

		public ServiceBuilder description(String description) {
			this.description = description;
			return this;
		}

		public ServiceBuilder siteId(String siteId) {
			this.siteId = siteId;
			return this;
		}

		public Service build() {
			return new Service(id, name, description, siteId);
		}
	}
}
