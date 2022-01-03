/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.domain.authn;

import io.imunity.furms.domain.FurmsEvent;
import io.imunity.furms.domain.users.FURMSUser;

import java.util.Objects;

public class UserLoggedOutEvent implements FurmsEvent {
	public final FURMSUser user;

	public UserLoggedOutEvent(FURMSUser user) {
		this.user = user;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		UserLoggedOutEvent that = (UserLoggedOutEvent) o;
		return Objects.equals(user, that.user);
	}

	@Override
	public int hashCode() {
		return Objects.hash(user);
	}

	@Override
	public String toString() {
		return "UserLoggedOutEvent{" +
			"user=" + user +
			'}';
	}
}
