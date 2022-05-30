/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.db.services;

import io.imunity.furms.db.id.uuid.UUIDIdentifiable;
import io.imunity.furms.domain.policy_documents.PolicyId;
import io.imunity.furms.domain.services.InfraService;
import io.imunity.furms.domain.services.InfraServiceId;
import io.imunity.furms.domain.sites.SiteId;
import org.springframework.data.relational.core.mapping.Table;

import java.util.Objects;
import java.util.UUID;

@Table("service")
class InfraServiceEntity extends UUIDIdentifiable {
	public final UUID siteId;
	public final String name;
	public final String description;
	public final UUID policyId;

	InfraServiceEntity(UUID id, UUID siteId, String name, String description, UUID policyId) {
		this.id = id;
		this.siteId = siteId;
		this.name = name;
		this.description = description;
		this.policyId = policyId;
	}

	InfraService toService(){
		return InfraService.builder()
			.id(new InfraServiceId(id))
			.name(name)
			.description(description)
			.siteId(new SiteId(siteId))
			.policyId(new PolicyId(policyId))
			.build();
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		InfraServiceEntity that = (InfraServiceEntity) o;
		return Objects.equals(id, that.id) &&
			Objects.equals(siteId, that.siteId) &&
			Objects.equals(name, that.name) &&
			Objects.equals(description, that.description) &&
			Objects.equals(policyId, that.policyId);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, siteId, name, description, policyId);
	}

	@Override
	public String toString() {
		return "ServiceEntity{" +
			"siteId=" + siteId +
			", id=" + id +
			", name='" + name + '\'' +
			", description='" + description + '\'' +
			", policyId=" + policyId +
			'}';
	}

	public static ServiceEntityBuilder builder() {
		return new ServiceEntityBuilder();
	}

	public static final class ServiceEntityBuilder {
		private UUID id;
		private UUID siteId;
		private String name;
		private String description;
		private UUID policyId;

		private ServiceEntityBuilder() {
		}

		public ServiceEntityBuilder siteId(UUID siteId) {
			this.siteId = siteId;
			return this;
		}

		public ServiceEntityBuilder policyId(UUID policyId) {
			this.policyId = policyId;
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

		public InfraServiceEntity build() {
			return new InfraServiceEntity(id, siteId, name, description, policyId);
		}
	}
}
