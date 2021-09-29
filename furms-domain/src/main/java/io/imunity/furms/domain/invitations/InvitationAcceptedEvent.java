/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.domain.invitations;

import io.imunity.furms.domain.authz.roles.ResourceId;
import io.imunity.furms.domain.users.FenixUserId;

import java.util.Objects;

public class InvitationAcceptedEvent implements InvitationEvent {
	public final FenixUserId id;
	public final String email;
	public final ResourceId resourceId;

	public InvitationAcceptedEvent(FenixUserId id, String email, ResourceId resourceId) {
		this.id = id;
		this.email = email;
		this.resourceId = resourceId;
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
		InvitationAcceptedEvent userEvent = (InvitationAcceptedEvent) o;
		return Objects.equals(id, userEvent.id) &&
			Objects.equals(email, userEvent.email) &&
			Objects.equals(resourceId, userEvent.resourceId);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, resourceId, email);
	}

	@Override
	public String toString() {
		return "InviteUserEvent{" +
			"id='" + id + '\'' +
			", resourceId=" + resourceId +
			", email=" + email +
			'}';
	}
}
