/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.domain.policy_documents;

import java.util.Objects;

public class ServicePolicyDocument {

	public final PolicyId id;
	public final String serviceId;
	public final String name;
	public final int revision;

	ServicePolicyDocument(PolicyId id, String serviceId, String name, int revision) {
		this.id = id;
		this.serviceId = serviceId;
		this.name = name;
		this.revision = revision;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		ServicePolicyDocument that = (ServicePolicyDocument) o;
		return revision == that.revision &&
			Objects.equals(id, that.id) &&
			Objects.equals(serviceId, that.serviceId) &&
			Objects.equals(name, that.name);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, name, serviceId, revision);
	}

	public static PolicyDocumentEntityBuilder builder() {
		return new PolicyDocumentEntityBuilder();
	}

	public static final class PolicyDocumentEntityBuilder {
		private PolicyId id;
		private String name;
		private String serviceId;
		private int revision;

		private PolicyDocumentEntityBuilder() {
		}

		public PolicyDocumentEntityBuilder id(PolicyId id) {
			this.id = id;
			return this;
		}

		public PolicyDocumentEntityBuilder serviceId(String serviceId) {
			this.serviceId = serviceId;
			return this;
		}

		public PolicyDocumentEntityBuilder name(String name) {
			this.name = name;
			return this;
		}

		public PolicyDocumentEntityBuilder revision(int revision) {
			this.revision = revision;
			return this;
		}

		public ServicePolicyDocument build() {
			return new ServicePolicyDocument(id, name, serviceId, revision);
		}
	}
}
