/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.domain.users;

import io.imunity.furms.domain.authz.roles.ResourceId;
import io.imunity.furms.domain.authz.roles.Role;

import java.util.Objects;

public class UserRoleGrantedByRegistrationEvent {
	public final String originatorEmail;
	public final ResourceId resourceId;
	public final String resourceName;
	public final Role role;
	public final String userEmail;

	public UserRoleGrantedByRegistrationEvent(String originatorEmail, ResourceId resourceId, String resourceName, Role role, String userEmail) {
		this.originatorEmail = originatorEmail;
		this.resourceId = resourceId;
		this.resourceName = resourceName;
		this.role = role;
		this.userEmail = userEmail;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		UserRoleGrantedByRegistrationEvent that = (UserRoleGrantedByRegistrationEvent) o;
		return Objects.equals(originatorEmail, that.originatorEmail) &&
			Objects.equals(resourceId, that.resourceId) &&
			Objects.equals(resourceName, that.resourceName) &&
			Objects.equals(userEmail, that.userEmail) &&
			role == that.role;
	}

	@Override
	public int hashCode() {
		return Objects.hash(originatorEmail, resourceId, resourceName, role, userEmail);
	}

	@Override
	public String toString() {
		return "UserRoleGrantedByRegistrationEvent{" +
			"orginatorId=" + originatorEmail +
			", resourceId=" + resourceId +
			", resourceName='" + resourceName + '\'' +
			", role=" + role +
			", userEmail=" + userEmail +
			'}';
	}
}
