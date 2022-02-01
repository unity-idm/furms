/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.domain.users;

import io.imunity.furms.domain.authz.roles.ResourceId;
import io.imunity.furms.domain.authz.roles.Role;

import java.util.Objects;

public class UserRoleGrantedByInvitationEvent implements UserEvent {
	public final String originatorEmail;
	public final PersistentId id;
	public final ResourceId resourceId;
	public final String resourceName;
	public final Role role;

	public UserRoleGrantedByInvitationEvent(String originatorEmail, PersistentId id, ResourceId resourceId, String resourceName, Role role) {
		this.originatorEmail = originatorEmail;
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
		UserRoleGrantedByInvitationEvent that = (UserRoleGrantedByInvitationEvent) o;
		return Objects.equals(originatorEmail, that.originatorEmail) && Objects.equals(id, that.id) && Objects.equals(resourceId, that.resourceId) && Objects.equals(resourceName, that.resourceName) && role == that.role;
	}

	@Override
	public int hashCode() {
		return Objects.hash(originatorEmail, id, resourceId, resourceName, role);
	}

	@Override
	public String toString() {
		return "UserRoleGrantedByInvitationEvent{" +
			"originatorEmail='" + originatorEmail + '\'' +
			", id=" + id +
			", resourceId=" + resourceId +
			", resourceName='" + resourceName + '\'' +
			", role=" + role +
			'}';
	}
}
