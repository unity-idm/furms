/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.domain.notification;

import io.imunity.furms.domain.FurmsEvent;
import io.imunity.furms.domain.users.FenixUserId;

import java.util.Objects;

public class UserPolicyNotificationRequestEvent implements FurmsEvent {
	public final FenixUserId fenixUserId;

	public UserPolicyNotificationRequestEvent(FenixUserId fenixUserId) {
		this.fenixUserId = fenixUserId;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		UserPolicyNotificationRequestEvent siteEvent = (UserPolicyNotificationRequestEvent) o;
			return Objects.equals(fenixUserId, siteEvent.fenixUserId);
	}

	@Override
	public int hashCode() {
		return Objects.hash(fenixUserId);
	}

	@Override
	public String toString() {
		return "PolicyDocumentCreateEvent{" +
			"fenixUserId='" + fenixUserId + '\'' +
			'}';
	}
}
