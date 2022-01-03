/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.domain.users;

import io.imunity.furms.domain.authz.roles.ResourceId;
import io.imunity.furms.domain.authz.roles.Role;

import java.util.Objects;

public class UserRoleGrantedByRegistrationEvent {
	public final FenixUserId originatorId;
	public final ResourceId resourceId;
	public final String resourceName;
	public final Role role;

	public UserRoleGrantedByRegistrationEvent(FenixUserId originatorId, ResourceId resourceId, String resourceName, Role role) {
		this.originatorId = originatorId;
		this.resourceId = resourceId;
		this.resourceName = resourceName;
		this.role = role;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		UserRoleGrantedByRegistrationEvent that = (UserRoleGrantedByRegistrationEvent) o;
		return Objects.equals(originatorId, that.originatorId) &&
			Objects.equals(resourceId, that.resourceId) &&
			Objects.equals(resourceName, that.resourceName) &&
			role == that.role;
	}

	@Override
	public int hashCode() {
		return Objects.hash(originatorId, resourceId, resourceName, role);
	}

	@Override
	public String toString() {
		return "UserRoleGrantedByRegistrationEvent{" +
			"orginatorId=" + originatorId +
			", resourceId=" + resourceId +
			", resourceName='" + resourceName + '\'' +
			", role=" + role +
			'}';
	}
}
