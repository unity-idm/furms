/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.api.alarms;

import io.imunity.furms.domain.alarms.UserActiveAlarm;

import java.util.Set;

public interface FiredAlarmsService {
	Set<UserActiveAlarm> findAllFiredAlarmsOfCurrentUser();
}
