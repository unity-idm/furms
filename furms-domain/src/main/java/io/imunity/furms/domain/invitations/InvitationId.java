/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.domain.invitations;

import java.util.UUID;

import io.imunity.furms.domain.UUIDBasedIdentifier;

public class InvitationId extends UUIDBasedIdentifier {

	public InvitationId(String id) {
		super(id);
	}

	public InvitationId(UUID id) {
		super(id);
	}

	public InvitationId(InvitationId id) {
		super(id);
	}

	@Override
	public String toString() {
		return "InvitationId{" + "id=" + id + '}';
	}
}
