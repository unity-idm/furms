/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.domain.alarms;

import io.imunity.furms.domain.Id;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public class AlarmId implements Id {
	public final UUID id;

	public AlarmId(UUID id) {
		this.id = id;
	}

	public AlarmId(AlarmId id) {
		this.id = Optional.ofNullable(id)
			.map(alarmId -> alarmId.id)
			.orElse(null);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		AlarmId alarmId = (AlarmId) o;
		return Objects.equals(id, alarmId.id);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id);
	}

	@Override
	public String toString() {
		return "AlarmId{" +
			"id=" + id +
			'}';
	}
}
