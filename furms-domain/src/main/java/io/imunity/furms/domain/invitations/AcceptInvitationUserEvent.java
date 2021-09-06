/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.domain.invitations;

import io.imunity.furms.domain.authz.roles.ResourceId;
import io.imunity.furms.domain.users.PersistentId;
import io.imunity.furms.domain.users.UserEvent;

import java.util.Objects;

public class AcceptInvitationUserEvent implements UserEvent {
	public final PersistentId id;
	public final ResourceId resourceId;

	public AcceptInvitationUserEvent(PersistentId id, ResourceId resourceId) {
		this.id = id;
		this.resourceId = resourceId;
	}

	@Override
	public PersistentId getId() {
		return id;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		AcceptInvitationUserEvent userEvent = (AcceptInvitationUserEvent) o;
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
