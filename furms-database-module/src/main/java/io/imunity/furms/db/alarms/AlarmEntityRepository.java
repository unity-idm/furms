/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.db.alarms;

import org.springframework.data.repository.CrudRepository;

import java.util.Set;
import java.util.UUID;

interface AlarmEntityRepository extends CrudRepository<AlarmEntity, UUID> {
	Set<AlarmEntity> findAllByProjectId(UUID projectId);
	boolean existsByIdAndProjectId(UUID id, UUID projectId);
}
