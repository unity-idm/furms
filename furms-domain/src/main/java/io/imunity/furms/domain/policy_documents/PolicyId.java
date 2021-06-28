/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.domain.policy_documents;

import java.util.Objects;
import java.util.UUID;

public class PolicyId {
	public final UUID id;

	public PolicyId(UUID id) {
		this.id = id;
	}

	public PolicyId(String id) {
		this.id = UUID.fromString(id);
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
}
