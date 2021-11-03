/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.domain.resource_access;

import io.imunity.furms.domain.FurmsEvent;

import java.util.Objects;

public class UserGrantRemovedEvent implements FurmsEvent {
	public final GrantAccess grantAccess;

	public UserGrantRemovedEvent(GrantAccess grantAccess) {
		this.grantAccess = grantAccess;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		UserGrantRemovedEvent that = (UserGrantRemovedEvent) o;
		return Objects.equals(grantAccess, that.grantAccess);
	}

	@Override
	public int hashCode() {
		return Objects.hash(grantAccess);
	}

	@Override
	public String toString() {
		return "UserGrantRemovedEvent{" +
			"grantAccess=" + grantAccess +
			'}';
	}
}
