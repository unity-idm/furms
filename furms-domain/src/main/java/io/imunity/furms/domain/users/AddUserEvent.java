/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.domain.users;

import io.imunity.furms.domain.authz.roles.ResourceId;

import java.util.Objects;

public class AddUserEvent implements UserEvent{
	public final PersistentId id;
	public final ResourceId resourceId;

	public AddUserEvent(PersistentId id, ResourceId resourceId) {
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
		AddUserEvent userEvent = (AddUserEvent) o;
		return Objects.equals(id, userEvent.id) &&
			Objects.equals(resourceId, userEvent.resourceId);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, resourceId);
	}

	@Override
	public String toString() {
		return "AddUserEvent{" +
			"id='" + id + '\'' +
			", resourceId=" + resourceId +
			'}';
	}
}
