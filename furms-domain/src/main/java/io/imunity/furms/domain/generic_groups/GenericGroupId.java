/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.domain.generic_groups;

import java.util.UUID;

import io.imunity.furms.domain.UUIDBasedIdentifier;

public class GenericGroupId extends UUIDBasedIdentifier {

	public GenericGroupId(String id) {
		super(id);
	}

	public GenericGroupId(UUID id) {
		super(id);
	}

	public GenericGroupId(GenericGroupId id) {
		super(id);
	}

	public static GenericGroupId empty() {
		return new GenericGroupId((UUID) null);
	}

	@Override
	public String toString() {
		return "GenericGroupId{" + "id=" + id + '}';
	}
}
