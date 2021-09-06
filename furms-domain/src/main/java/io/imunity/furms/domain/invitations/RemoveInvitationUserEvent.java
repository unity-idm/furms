/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.domain.invitations;

import io.imunity.furms.domain.users.PersistentId;
import io.imunity.furms.domain.users.UserEvent;

import java.util.Objects;

public class RemoveInvitationUserEvent implements UserEvent {
	public final PersistentId id;
	public final InvitationId invitationId;
	public final InvitationCode code;

	public RemoveInvitationUserEvent(PersistentId id, InvitationId invitationId) {
		this.id = id;
		this.invitationId = invitationId;
		this.code = null;
	}

	public RemoveInvitationUserEvent(PersistentId id, InvitationCode invitationCode) {
		this.id = id;
		this.code = invitationCode;
		this.invitationId = null;
	}

	@Override
	public PersistentId getId() {
		return id;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		RemoveInvitationUserEvent userEvent = (RemoveInvitationUserEvent) o;
		return Objects.equals(id, userEvent.id) &&
			Objects.equals(invitationId, userEvent.invitationId);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, invitationId);
	}

	@Override
	public String toString() {
		return "InviteUserEvent{" +
			"id='" + id + '\'' +
			", invitationId=" + invitationId +
			'}';
	}
}
