/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.domain.alarms;

import io.imunity.furms.domain.authz.roles.Role;
import io.imunity.furms.domain.users.FenixUserId;

import java.util.Objects;
import java.util.Set;

public class UserActiveAlarm {
	public final FiredAlarm alarm;
	public final FenixUserId userId;
	public final Set<Role> roles;

	public UserActiveAlarm(FiredAlarm alarm, FenixUserId userId, Set<Role> roles) {
		this.alarm = alarm;
		this.userId = userId;
		this.roles = roles != null ? roles : Set.of();
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		UserActiveAlarm that = (UserActiveAlarm) o;
		return Objects.equals(alarm, that.alarm) && Objects.equals(userId, that.userId) && Objects.equals(roles, that.roles);
	}

	@Override
	public int hashCode() {
		return Objects.hash(alarm, userId, roles);
	}

	@Override
	public String toString() {
		return "UserActiveAlarm{" +
			"alarm=" + alarm +
			", userId=" + userId +
			", roles=" + roles +
			'}';
	}
}
