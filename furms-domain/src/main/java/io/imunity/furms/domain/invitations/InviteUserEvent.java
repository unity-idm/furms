/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.domain.invitations;

import io.imunity.furms.domain.authz.roles.ResourceId;
import io.imunity.furms.domain.users.FenixUserId;

import java.util.Objects;

public class InviteUserEvent implements InvitationEvent {
	public final FenixUserId id;
	public final ResourceId resourceId;

	public InviteUserEvent(FenixUserId id, ResourceId resourceId) {
		this.id = id;
		this.resourceId = resourceId;
	}

	@Override
	public FenixUserId getId() {
		return id;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		InviteUserEvent userEvent = (InviteUserEvent) o;
		return Objects.equals(id, userEvent.id) &&
			Objects.equals(resourceId, userEvent.resourceId);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, resourceId);
	}

	@Override
	public String toString() {
		return "InviteUserEvent{" +
			"id='" + id + '\'' +
			", resourceId=" + resourceId +
			'}';
	}
}
