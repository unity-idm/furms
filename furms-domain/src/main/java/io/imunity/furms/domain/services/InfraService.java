/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.domain.services;

import io.imunity.furms.domain.policy_documents.PolicyId;
import io.imunity.furms.domain.sites.SiteId;

import java.util.Objects;

public class InfraService {
	public final InfraServiceId id;
	public final String name;
	public final String description;
	public final SiteId siteId;
	public final PolicyId policyId;

	public InfraService(InfraServiceId id, String name, String description, SiteId siteId, PolicyId policyId) {
		this.id = id;
		this.name = name;
		this.description = description;
		this.siteId = siteId;
		this.policyId = policyId;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		InfraService infraService = (InfraService) o;
		return Objects.equals(id, infraService.id) &&
			Objects.equals(name, infraService.name) &&
			Objects.equals(description, infraService.description) &&
			Objects.equals(siteId, infraService.siteId) &&
			Objects.equals(policyId, infraService.policyId);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, name, description, siteId, policyId);
	}

	@Override
	public String toString() {
		return "InfraService{" +
			"id='" + id + '\'' +
			", name='" + name + '\'' +
			", description='" + description + '\'' +
			", siteId='" + siteId + '\'' +
			", policyId='" + policyId + '\'' +
			'}';
	}

	public static ServiceBuilder builder() {
		return new ServiceBuilder();
	}

	public static final class ServiceBuilder {
		public InfraServiceId id;
		public String name;
		public String description;
		public SiteId siteId;
		public PolicyId policyId = PolicyId.empty();

		private ServiceBuilder() {
		}

		public ServiceBuilder id(InfraServiceId id) {
			this.id = id;
			return this;
		}

		public ServiceBuilder policyId(PolicyId policyId) {
			this.policyId = policyId;
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

		public ServiceBuilder siteId(SiteId siteId) {
			this.siteId = siteId;
			return this;
		}

		public InfraService build() {
			return new InfraService(id, name, description, siteId, policyId);
		}
	}
}
