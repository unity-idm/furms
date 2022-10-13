/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.domain.resource_access;

import java.util.UUID;

import io.imunity.furms.domain.UUIDBasedIdentifier;

public class GrantId extends UUIDBasedIdentifier {

	public GrantId(String id) {
		super(id);
	}

	public GrantId(UUID id) {
		super(id);
	}

	public GrantId(GrantId id) {
		super(id);
	}

	@Override
	public String toString() {
		return "GrantId{" + "id=" + id + '}';
	}
}
