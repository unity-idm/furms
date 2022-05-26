/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.domain.invitations;

import io.imunity.furms.domain.Id;

import java.util.Objects;
import java.util.UUID;

public class InvitationId implements Id {
	public final UUID id;

	public InvitationId(UUID id) {
		this.id = id;
	}

	public InvitationId(String id) {
		this.id = UUID.fromString(id);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		InvitationId that = (InvitationId) o;
		return Objects.equals(id, that.id);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id);
	}

	@Override
	public String toString() {
		return "InvitationId{" +
			"id=" + id +
			'}';
	}

	@Override
	public UUID getId() {
		return id;
	}
}
