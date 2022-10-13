/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.domain.services;

import java.util.UUID;

import io.imunity.furms.domain.UUIDBasedIdentifier;

public class InfraServiceId extends UUIDBasedIdentifier {

	public InfraServiceId(String id) {
		super(id);
	}

	public InfraServiceId(UUID id) {
		super(id);
	}

	public InfraServiceId(InfraServiceId id) {
		super(id);
	}

	@Override
	public String toString() {
		return "InfraServiceId{" + "id=" + id + '}';
	}
}
