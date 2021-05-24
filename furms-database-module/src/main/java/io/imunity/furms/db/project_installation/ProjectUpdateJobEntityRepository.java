/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.db.project_installation;

import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProjectUpdateJobEntityRepository extends CrudRepository<ProjectUpdateJobEntity, UUID> {
	Optional<ProjectUpdateJobEntity> findByCorrelationId(UUID correlationId);
	List<ProjectUpdateJobEntity> findByProjectId(UUID projectId);
	boolean existsByProjectIdAndStatusOrProjectIdAndStatus(UUID projectId, int pendingStatus, UUID projectId1, int ackStatus);

}
