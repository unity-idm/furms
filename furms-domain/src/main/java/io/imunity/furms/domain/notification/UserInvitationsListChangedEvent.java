/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.domain.notification;

import io.imunity.furms.domain.FurmsEvent;

import java.util.Objects;

public class UserInvitationsListChangedEvent implements FurmsEvent {
	public final String email;

	public UserInvitationsListChangedEvent(String email) {
		this.email = email;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		UserInvitationsListChangedEvent that = (UserInvitationsListChangedEvent) o;
		return Objects.equals(email, that.email);
	}

	@Override
	public int hashCode() {
		return Objects.hash(email);
	}

	@Override
	public String toString() {
		return "UserInvitationNotificationRequestEvent{" +
			"email='" + email + '\'' +
			'}';
	}
}
