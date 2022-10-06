/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.domain.policy_documents;

import java.util.Objects;
import java.util.UUID;

import io.imunity.furms.domain.Id;

public class PolicyId implements Id {
	public final UUID id;

	public PolicyId(UUID id) {
		this.id = id;
	}

	public PolicyId(String id) {
		this.id = UUID.fromString(id);
	}

	public static PolicyId empty() {
		return new PolicyId((UUID) null);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		PolicyId policyId = (PolicyId) o;
		return Objects.equals(id, policyId.id);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id);
	}

	@Override
	public String toString() {
		return "PolicyId{" +
			"id=" + id +
			'}';
	}
	
	@Override
	public String asRawString() {
		return RawIdParser.asRawString(id);
	}
}
