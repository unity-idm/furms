/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.domain.invitations;

import io.imunity.furms.domain.users.FenixUserId;

import java.util.Objects;

public class RemoveInvitationUserEvent implements InvitationEvent {
	public final FenixUserId id;
	public final String email;
	public final InvitationId invitationId;

	public RemoveInvitationUserEvent(FenixUserId id, String email, InvitationId invitationId) {
		this.id = id;
		this.email = email;
		this.invitationId = invitationId;
	}

	public FenixUserId getId() {
		return id;
	}

	@Override
	public String getEmail() {
		return email;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		RemoveInvitationUserEvent that = (RemoveInvitationUserEvent) o;
		return Objects.equals(id, that.id) &&
			Objects.equals(invitationId, that.invitationId) &&
			Objects.equals(email, that.email);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, invitationId, email);
	}

	@Override
	public String toString() {
		return "RemoveInvitationUserEvent{" +
			"id=" + id +
			", invitationId=" + invitationId +
			", email=" + email +
			'}';
	}
}
