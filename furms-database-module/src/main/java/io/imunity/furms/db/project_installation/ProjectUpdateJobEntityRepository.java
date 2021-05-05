/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.db.project_installation;

import org.springframework.data.repository.CrudRepository;

import java.util.UUID;

public interface ProjectUpdateJobEntityRepository extends CrudRepository<ProjectUpdateJobEntity, UUID> {
	ProjectUpdateJobEntity findByCorrelationId(UUID correlationId);
	boolean existsByProjectId(UUID projectId);
}
