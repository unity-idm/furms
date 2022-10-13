/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.domain.community_allocation;

import java.util.UUID;

import io.imunity.furms.domain.UUIDBasedIdentifier;

public class CommunityAllocationId extends UUIDBasedIdentifier {

	public CommunityAllocationId(String id) {
		super(id);
	}

	public CommunityAllocationId(UUID id) {
		super(id);
	}

	public CommunityAllocationId(CommunityAllocationId id) {
		super(id);
	}

	@Override
	public String toString() {
		return "CommunityAllocationId{" + "id=" + id + '}';
	}
}
