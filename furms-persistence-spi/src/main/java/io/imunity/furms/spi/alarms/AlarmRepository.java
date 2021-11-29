/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.spi.alarms;

import io.imunity.furms.domain.alarms.ActiveAlarm;
import io.imunity.furms.domain.alarms.AlarmWithUserIds;
import io.imunity.furms.domain.alarms.AlarmId;
import io.imunity.furms.domain.users.FenixUserId;

import java.util.Optional;
import java.util.Set;

public interface AlarmRepository {
	Set<AlarmWithUserIds> findAll(String projectId);
	Set<ActiveAlarm> findAll(FenixUserId userId);
	Optional<AlarmWithUserIds> find(AlarmId id);
	Optional<AlarmWithUserIds> find(String projectAllocationId);
	AlarmId create(AlarmWithUserIds alarm);
	void update(AlarmWithUserIds alarm);
	void remove(AlarmId id);
	boolean exist(String projectId, AlarmId id);
	boolean exist(String projectId, String name);
}
