/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.domain.resource_credits;

import java.util.UUID;

import io.imunity.furms.domain.UUIDBasedIdentifier;

public class ResourceCreditId extends UUIDBasedIdentifier {

	public ResourceCreditId(String id) {
		super(id);
	}

	public ResourceCreditId(UUID id) {
		super(id);
	}

	public ResourceCreditId(ResourceCreditId id) {
		super(id);
	}

	@Override
	public String toString() {
		return "ResourceCreditId{" + "id=" + id + '}';
	}
}
