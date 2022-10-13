/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.domain.communities;

import java.util.UUID;

import io.imunity.furms.domain.UUIDBasedIdentifier;

public class CommunityId extends UUIDBasedIdentifier {

	public CommunityId(String id) {
		super(id);
	}

	public CommunityId(UUID id) {
		super(id);
	}

	public CommunityId(CommunityId id) {
		super(id);
	}

	@Override
	public String toString() {
		return "CommunityId{" + "id=" + id + '}';
	}
}
