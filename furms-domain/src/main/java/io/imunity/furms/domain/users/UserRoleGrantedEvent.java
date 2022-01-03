/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.domain.users;

import io.imunity.furms.domain.authz.roles.ResourceId;
import io.imunity.furms.domain.authz.roles.Role;

import java.util.Objects;

public class UserRoleGrantedEvent implements UserEvent {
	public final PersistentId id;
	public final ResourceId resourceId;
	public final String resourceName;
	public final Role role;

	public UserRoleGrantedEvent(PersistentId id, ResourceId resourceId, String resourceName, Role role) {
		this.id = id;
		this.resourceId = resourceId;
		this.resourceName = resourceName;
		this.role = role;
	}

	@Override
	public PersistentId getId() {
		return id;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		UserRoleGrantedEvent that = (UserRoleGrantedEvent) o;
		return Objects.equals(id, that.id) &&
			Objects.equals(resourceId, that.resourceId) &&
			Objects.equals(resourceName, that.resourceName) &&
			role == that.role;
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, resourceId, resourceName, role);
	}

	@Override
	public String toString() {
		return "UserRoleGrantedEvent{" +
			"id=" + id +
			", resourceId=" + resourceId +
			", resourceName='" + resourceName + '\'' +
			", role=" + role +
			'}';
	}
}
