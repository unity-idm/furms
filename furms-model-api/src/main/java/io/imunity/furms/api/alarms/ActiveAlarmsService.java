/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.api.alarms;

import io.imunity.furms.domain.alarms.ActiveAlarm;

import java.util.Set;

public interface ActiveAlarmsService {
	Set<ActiveAlarm> findAllActiveAlarmsAssignToCurrentUser();
}
