/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.views.user_settings.invitations;

import io.imunity.furms.domain.invitations.InvitationId;

import java.time.LocalDateTime;
import java.util.Objects;

class InvitationGridModel {
	public final InvitationId id;
	public final String invitationText;
	public final String originator;
	public final LocalDateTime expiration;

	InvitationGridModel(InvitationId id, String invitationText, String originator, LocalDateTime expiration) {
		this.id = id;
		this.invitationText = invitationText;
		this.originator = originator;
		this.expiration = expiration;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		InvitationGridModel that = (InvitationGridModel) o;
		return Objects.equals(id, that.id);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id);
	}

	@Override
	public String toString() {
		return "InvitationGridModel{" +
			"id=" + id +
			", invitationText='" + invitationText + '\'' +
			", originator='" + originator + '\'' +
			", expiration=" + expiration +
			'}';
	}
}
