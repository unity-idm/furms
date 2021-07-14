/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.views.site.services;

import io.imunity.furms.domain.policy_documents.PolicyId;

import java.util.Objects;

class InfraServiceViewModel {
	private final String id;
	private final String siteId;
	private String name;
	private String description;
	private PolicyId policyId;

	private InfraServiceViewModel(String id, String siteId, String name, String description, PolicyId policyId) {
		this.id = id;
		this.siteId = siteId;
		this.name = name;
		this.description = description;
		this.policyId = policyId;
	}

	InfraServiceViewModel(String siteId) {
		this.id = null;
		this.siteId = siteId;
		this.policyId = PolicyId.empty();
	}

	public String getId() {
		return id;
	}

	public String getSiteId() {
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
		private String id;
		private String siteId;
		private String name;
		private String description;
		private PolicyId policyId = PolicyId.empty();

		private ServiceViewModelBuilder() {
		}

		public ServiceViewModelBuilder id(String id) {
			this.id = id;
			return this;
		}

		public ServiceViewModelBuilder policyId(PolicyId policyId) {
			this.policyId = policyId;
			return this;
		}

		public ServiceViewModelBuilder siteId(String siteId) {
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
