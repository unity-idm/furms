/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.domain.policy_documents;

import io.imunity.furms.domain.services.InfraServiceId;

import java.util.Objects;
import java.util.Optional;

public class AssignedPolicyDocument {

	public final PolicyId id;
	public final Optional<InfraServiceId> serviceId;
	public final String name;
	public final int revision;

	public AssignedPolicyDocument(PolicyId id, Optional<InfraServiceId> serviceId, String name, int revision) {
		this.id = id;
		this.serviceId = serviceId;
		this.name = name;
		this.revision = revision;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		AssignedPolicyDocument that = (AssignedPolicyDocument) o;
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
		private Optional<InfraServiceId> serviceId;
		private int revision;

		private PolicyDocumentEntityBuilder() {
		}

		public PolicyDocumentEntityBuilder id(PolicyId id) {
			this.id = id;
			return this;
		}

		public PolicyDocumentEntityBuilder serviceId(InfraServiceId serviceId) {
			this.serviceId = Optional.ofNullable(serviceId);
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

		public AssignedPolicyDocument build() {
			return new AssignedPolicyDocument(id, serviceId, name, revision);
		}
	}
}
