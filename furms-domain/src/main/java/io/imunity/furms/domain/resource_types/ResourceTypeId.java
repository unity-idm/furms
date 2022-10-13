/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.domain.resource_types;

import java.util.UUID;

import io.imunity.furms.domain.UUIDBasedIdentifier;

public class ResourceTypeId extends UUIDBasedIdentifier {

	public ResourceTypeId(String id) {
		super(id);
	}

	public ResourceTypeId(UUID id) {
		super(id);
	}

	public ResourceTypeId(ResourceTypeId id) {
		super(id);
	}

	@Override
	public String toString() {
		return "ResourceTypeId{" + "id=" + id + '}';
	}
}
