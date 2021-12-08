/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.domain.alarms;

import io.imunity.furms.domain.FurmsEvent;

import java.util.Objects;

public class AlarmUpdatedEvent implements FurmsEvent {
	public final AlarmWithUserIds newAlarm;
	public final AlarmWithUserIds oldAlarm;

	public AlarmUpdatedEvent(AlarmWithUserIds newAlarm, AlarmWithUserIds oldAlarm) {
		this.newAlarm = newAlarm;
		this.oldAlarm = oldAlarm;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		AlarmUpdatedEvent that = (AlarmUpdatedEvent) o;
		return Objects.equals(newAlarm, that.newAlarm) && Objects.equals(oldAlarm, that.oldAlarm);
	}

	@Override
	public int hashCode() {
		return Objects.hash(newAlarm, oldAlarm);
	}

	@Override
	public String toString() {
		return "AlarmUpdatedEvent{" +
			"newAlarm=" + newAlarm +
			", oldAlarm=" + oldAlarm +
			'}';
	}
}
