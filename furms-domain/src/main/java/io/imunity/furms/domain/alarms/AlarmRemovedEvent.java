/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.domain.alarms;

import io.imunity.furms.domain.FurmsEvent;

import java.util.Objects;

public class AlarmRemovedEvent implements FurmsEvent {
	public final AlarmWithUserIds alarm;

	public AlarmRemovedEvent(AlarmWithUserIds alarm) {
		this.alarm = alarm;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		AlarmRemovedEvent that = (AlarmRemovedEvent) o;
		return Objects.equals(alarm, that.alarm);
	}

	@Override
	public int hashCode() {
		return Objects.hash(alarm);
	}

	@Override
	public String toString() {
		return "AlarmRemovedEvent{" +
			"alarm=" + alarm +
			'}';
	}
}
