/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.api.alarms;

import io.imunity.furms.domain.alarms.AlarmId;
import io.imunity.furms.domain.alarms.AlarmWithUserEmails;
import io.imunity.furms.domain.project_allocation.ProjectAllocationId;
import io.imunity.furms.domain.projects.ProjectId;

import java.util.Optional;
import java.util.Set;

public interface AlarmService {
	Set<AlarmWithUserEmails> findAll(ProjectId projectId);
	Optional<AlarmWithUserEmails> find(ProjectId projectId, AlarmId id);
	Optional<AlarmWithUserEmails> find(ProjectId projectId, ProjectAllocationId projectAllocationId);
	void create(AlarmWithUserEmails alarm);
	void update(AlarmWithUserEmails alarm);
	void remove(ProjectId projectId, AlarmId id);
}
