/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.db.project_installation;

import org.springframework.data.jdbc.repository.query.Modifying;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public interface ProjectUpdateJobEntityRepository extends CrudRepository<ProjectUpdateJobEntity, UUID> {
	Optional<ProjectUpdateJobEntity> findByCorrelationId(UUID correlationId);
	Optional<ProjectUpdateJobEntity> findByProjectIdAndSiteId(UUID projectId, UUID siteId);
	List<ProjectUpdateJobEntity> findByProjectId(UUID projectId);
	boolean existsByProjectIdAndStatusOrProjectIdAndStatus(UUID projectId, int pendingStatus, UUID projectId1, int ackStatus);

	@Query(
		"select puj.* " +
			"from project_update_job puj " +
			"join project p on puj.project_id = p.id " +
			"join community c on p.community_id = c.id " +
			"where c.id = :id")
	Set<ProjectUpdateJobEntity> findAllByCommunityId(@Param("id") UUID communityId);

	@Modifying
	@Query("delete from project_update_job where correlation_id = :correlation_id")
	void deleteByCorrelationId(@Param("correlation_id") UUID correlationId);
}
