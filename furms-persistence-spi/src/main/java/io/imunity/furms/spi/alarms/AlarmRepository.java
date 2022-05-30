/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.spi.alarms;

import io.imunity.furms.domain.alarms.AlarmId;
import io.imunity.furms.domain.alarms.AlarmWithUserIds;
import io.imunity.furms.domain.alarms.FiredAlarm;
import io.imunity.furms.domain.project_allocation.ProjectAllocationId;
import io.imunity.furms.domain.projects.ProjectId;
import io.imunity.furms.domain.users.FenixUserId;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface AlarmRepository {
	Set<AlarmWithUserIds> findAll(ProjectId projectId);
	Set<FiredAlarm> findAll(List<ProjectId> projectIds, FenixUserId userId);
	Optional<AlarmWithUserIds> find(AlarmId id);
	Optional<AlarmWithUserIds> find(ProjectAllocationId projectAllocationId);
	AlarmId create(AlarmWithUserIds alarm);
	void update(AlarmWithUserIds alarm);
	void updateToFired(AlarmWithUserIds alarm);
	void remove(AlarmId id);
	boolean exist(ProjectId projectId, AlarmId id);
	boolean exist(ProjectId projectId, String name);
}
