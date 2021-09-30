/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.domain.invitations;

import io.imunity.furms.domain.users.FenixUserId;

import java.util.Objects;

public class UpdateInvitationUserEvent implements InvitationEvent {
	public final FenixUserId id;
	public final String email;
	public final InvitationId invitationId;

	public UpdateInvitationUserEvent(FenixUserId id, String email, InvitationId invitationId) {
		this.id = id;
		this.email = email;
		this.invitationId = invitationId;
	}

	@Override
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
		UpdateInvitationUserEvent userEvent = (UpdateInvitationUserEvent) o;
		return Objects.equals(id, userEvent.id) &&
			Objects.equals(email, userEvent.email) &&
			Objects.equals(invitationId, userEvent.invitationId);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, invitationId, email);
	}

	@Override
	public String toString() {
		return "InviteUserEvent{" +
			"id='" + id + '\'' +
			", invitationId=" + invitationId +
			", email=" + email +
			'}';
	}
}
