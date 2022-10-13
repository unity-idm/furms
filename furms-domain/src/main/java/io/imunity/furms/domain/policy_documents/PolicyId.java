/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.domain.policy_documents;

import java.util.UUID;

import io.imunity.furms.domain.UUIDBasedIdentifier;

public class PolicyId extends UUIDBasedIdentifier {

	public PolicyId(String id) {
		super(id);
	}

	public PolicyId(UUID id) {
		super(id);
	}

	public PolicyId(PolicyId id) {
		super(id);
	}
	
	public static PolicyId empty() {
		return new PolicyId((UUID) null);
	}

	@Override
	public String toString() {
		return "PolicyId{" + "id=" + id + '}';
	}
}
