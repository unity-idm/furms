/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.api.alarms;

import io.imunity.furms.domain.alarms.AlarmId;
import io.imunity.furms.domain.alarms.AlarmWithUserEmails;

import java.util.Optional;
import java.util.Set;

public interface AlarmService {
	Set<AlarmWithUserEmails> findAll(String projectId);
	Optional<AlarmWithUserEmails> find(String projectId, AlarmId id);
	Optional<AlarmWithUserEmails> find(String projectId, String projectAllocationId);
	void create(AlarmWithUserEmails alarm);
	void update(AlarmWithUserEmails alarm);
	void remove(String projectId, AlarmId id);
}
