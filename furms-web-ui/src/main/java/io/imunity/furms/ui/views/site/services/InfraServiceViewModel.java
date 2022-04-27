/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.views.site.services;

import io.imunity.furms.domain.policy_documents.PolicyId;
import io.imunity.furms.domain.services.InfraServiceId;
import io.imunity.furms.domain.sites.SiteId;

import java.util.Objects;
import java.util.UUID;

class InfraServiceViewModel {
	private final InfraServiceId id;
	private final SiteId siteId;
	private String name;
	private String description;
	private PolicyId policyId;

	private InfraServiceViewModel(InfraServiceId id, SiteId siteId, String name, String description, PolicyId policyId) {
		this.id = id;
		this.siteId = siteId;
		this.name = name;
		this.description = description;
		this.policyId = policyId;
	}

	InfraServiceViewModel(SiteId siteId) {
		this.id = new InfraServiceId((UUID) null);
		this.siteId = siteId;
		this.policyId = PolicyId.empty();
	}

	public InfraServiceId getId() {
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

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	PolicyId getPolicyId() {
		return policyId;
	}

	void setPolicyId(PolicyId policyId) {
		this.policyId = policyId;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		InfraServiceViewModel that = (InfraServiceViewModel) o;
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
		return "ServiceViewModel{" +
			"id='" + id + '\'' +
			", siteId='" + siteId + '\'' +
			", name='" + name + '\'' +
			", description='" + description + '\'' +
			", policyId='" + policyId + '\'' +
			'}';
	}

	public static ServiceViewModelBuilder builder() {
		return new ServiceViewModelBuilder();
	}

	public static final class ServiceViewModelBuilder {
		private InfraServiceId id;
		private SiteId siteId;
		private String name;
		private String description;
		private PolicyId policyId = PolicyId.empty();

		private ServiceViewModelBuilder() {
		}

		public ServiceViewModelBuilder id(InfraServiceId id) {
			this.id = id;
			return this;
		}

		public ServiceViewModelBuilder policyId(PolicyId policyId) {
			this.policyId = policyId;
			return this;
		}

		public ServiceViewModelBuilder siteId(SiteId siteId) {
			this.siteId = siteId;
			return this;
		}

		public ServiceViewModelBuilder name(String name) {
			this.name = name;
			return this;
		}

		public ServiceViewModelBuilder description(String description) {
			this.description = description;
			return this;
		}

		public InfraServiceViewModel build() {
			return new InfraServiceViewModel(id, siteId, name, description, policyId);
		}
	}
}
