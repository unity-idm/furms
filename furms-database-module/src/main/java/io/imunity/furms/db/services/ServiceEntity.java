/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.db.services;

import io.imunity.furms.db.id.uuid.UUIDIdentifiable;
import io.imunity.furms.domain.services.Service;
import org.springframework.data.relational.core.mapping.Table;

import java.util.Objects;
import java.util.UUID;

@Table("service")
public class ServiceEntity extends UUIDIdentifiable {
	public final UUID siteId;
	public final String name;
	public final String description;

	public ServiceEntity(UUID id, UUID siteId, String name, String description) {
		this.id = id;
		this.siteId = siteId;
		this.name = name;
		this.description = description;
	}

	public Service toService(){
		return Service.builder()
			.id(id.toString())
			.name(name)
			.description(description)
			.siteId(siteId.toString())
			.build();
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		ServiceEntity that = (ServiceEntity) o;
		return Objects.equals(id, that.id) &&
			Objects.equals(siteId, that.siteId) &&
			Objects.equals(name, that.name) &&
			Objects.equals(description, that.description);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, siteId, name, description);
	}

	@Override
	public String toString() {
		return "ServiceEntity{" +
			"siteId=" + siteId +
			", name='" + name + '\'' +
			", description='" + description + '\'' +
			", id=" + id +
			'}';
	}

	public static ServiceEntityBuilder builder() {
		return new ServiceEntityBuilder();
	}

	public static final class ServiceEntityBuilder {
		protected UUID id;
		public UUID siteId;
		public String name;
		public String description;

		private ServiceEntityBuilder() {
		}

		public ServiceEntityBuilder siteId(UUID siteId) {
			this.siteId = siteId;
			return this;
		}

		public ServiceEntityBuilder name(String name) {
			this.name = name;
			return this;
		}

		public ServiceEntityBuilder description(String description) {
			this.description = description;
			return this;
		}

		public ServiceEntityBuilder id(UUID id) {
			this.id = id;
			return this;
		}

		public ServiceEntity build() {
			return new ServiceEntity(id, siteId, name, description);
		}
	}
}
