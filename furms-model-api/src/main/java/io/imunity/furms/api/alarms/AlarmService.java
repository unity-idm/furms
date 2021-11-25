/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.api.alarms;

import io.imunity.furms.domain.alarms.Alarm;
import io.imunity.furms.domain.alarms.AlarmId;

import java.util.Optional;
import java.util.Set;

public interface AlarmService {
	Set<Alarm> findAll(String projectId);
	Optional<Alarm> find(String projectId, AlarmId id);
	void create(Alarm alarm);
	void update(Alarm alarm);
	void remove(String projectId, AlarmId id);
}
