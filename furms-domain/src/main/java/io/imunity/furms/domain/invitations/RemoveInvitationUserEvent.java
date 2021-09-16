/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.domain.invitations;

import io.imunity.furms.domain.users.FenixUserId;

import java.util.Objects;

public class RemoveInvitationUserEvent implements InvitationEvent {
	public final FenixUserId id;
	public final InvitationId invitationId;
	public final InvitationCode code;

	public RemoveInvitationUserEvent(FenixUserId id, InvitationId invitationId, InvitationCode code) {
		this.id = id;
		this.invitationId = invitationId;
		this.code = code;
	}

	public FenixUserId getId() {
		return id;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		RemoveInvitationUserEvent that = (RemoveInvitationUserEvent) o;
		return Objects.equals(id, that.id) &&
			Objects.equals(invitationId, that.invitationId) &&
			Objects.equals(code, that.code);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, invitationId, code);
	}

	@Override
	public String toString() {
		return "RemoveInvitationUserEvent{" +
			"id=" + id +
			", invitationId=" + invitationId +
			", code=" + code +
			'}';
	}
}
