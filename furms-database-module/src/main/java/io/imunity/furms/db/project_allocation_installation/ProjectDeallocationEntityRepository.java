/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.db.project_allocation_installation;

import org.springframework.data.jdbc.repository.query.Modifying;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public interface ProjectDeallocationEntityRepository extends CrudRepository<ProjectDeallocationEntity, UUID> {
	Optional<ProjectDeallocationEntity> findByCorrelationId(UUID correlationId);

	Optional<ProjectDeallocationEntity> findByProjectAllocationId(UUID correlationId);


	@Query("select pd.* " +
		"from project_deallocation pd " +
		"join project_allocation pa on pa.id = pd.project_allocation_id " +
		"where pa.project_id = :id")
	Set<ProjectDeallocationEntity> findAllByProjectId(@Param("id") UUID projectId);

	@Modifying
	@Query("delete from project_deallocation where correlation_id = :correlation_id")
	void deleteByCorrelationId(@Param("correlation_id") UUID correlationId);
}
