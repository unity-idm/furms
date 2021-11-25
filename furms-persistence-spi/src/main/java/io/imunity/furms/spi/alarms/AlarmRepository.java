/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.spi.alarms;

import io.imunity.furms.domain.alarms.Alarm;
import io.imunity.furms.domain.alarms.AlarmId;

import java.util.Optional;
import java.util.Set;

public interface AlarmRepository {
	Set<Alarm> findAll(String projectId);
	Optional<Alarm> find(AlarmId id);
	AlarmId create(Alarm alarm);
	void update(Alarm alarm);
	void remove(AlarmId id);
	boolean exist(String projectId, AlarmId id);
}
