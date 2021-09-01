/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */
package io.imunity.furms.rest.admin;

import io.imunity.furms.domain.policy_documents.PolicyDocument;

import java.util.Objects;

class Policy {
	public final String policyId;
	public final String name;
	public final int revision;

	Policy(PolicyDocument policyDocument) {
		this.policyId = policyDocument.id.id.toString();
		this.name = policyDocument.name;
		this.revision = policyDocument.revision;
	}

	Policy(String id, String name, int revision) {
		this.policyId = id;
		this.name = name;
		this.revision = revision;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		Policy policy = (Policy) o;
		return revision == policy.revision
				&& Objects.equals(policyId, policy.policyId)
				&& Objects.equals(name, policy.name);
	}

	@Override
	public int hashCode() {
		return Objects.hash(policyId, name, revision);
	}

	@Override
	public String toString() {
		return "Policy{" +
				"id=" + policyId +
				", name='" + name + '\'' +
				", revision=" + revision +
				'}';
	}
}
