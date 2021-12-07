/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.domain.notification;

import io.imunity.furms.domain.FurmsEvent;
import io.imunity.furms.domain.users.FenixUserId;

import java.util.Objects;

public class UserAlarmListChangedEvent implements FurmsEvent {
	public final FenixUserId fenixUserId;

	public UserAlarmListChangedEvent(FenixUserId fenixUserId) {
		this.fenixUserId = fenixUserId;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		UserAlarmListChangedEvent siteEvent = (UserAlarmListChangedEvent) o;
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
