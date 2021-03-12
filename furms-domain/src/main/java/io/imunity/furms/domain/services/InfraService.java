/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.domain.services;

import java.util.Objects;

public class InfraService {
	public final String id;
	public final String name;
	public final String description;
	public final String siteId;

	public InfraService(String id, String name, String description, String siteId) {
		this.id = id;
		this.name = name;
		this.description = description;
		this.siteId = siteId;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		InfraService infraService = (InfraService) o;
		return Objects.equals(id, infraService.id) &&
			Objects.equals(name, infraService.name) &&
			Objects.equals(description, infraService.description) &&
			Objects.equals(siteId, infraService.siteId);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, name, description, siteId);
	}

	@Override
	public String toString() {
		return "InfraService{" +
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

		public InfraService build() {
			return new InfraService(id, name, description, siteId);
		}
	}
}
