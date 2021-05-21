/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.db.user_operation;

import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public interface UserAdditionEntityRepository extends CrudRepository<UserAdditionSaveEntity, UUID> {
	@Query(
			"select ua.*, uaj.*, s.id as site_id, s.external_id as site_external_id " +
			"from user_addition ua " +
			"join user_addition_job uaj on ua.id = uaj.user_addition_id " +
			"join site s on ua.site_id = s.id " +
			"where ua.project_id = :project_id and ua.user_id = :user_id"
	)
	Set<UserAdditionReadEntity> findAllByProjectIdAndUserId(@Param("project_id") UUID projectId, @Param("user_id") String userId);

	@Query("select ua.*, uaj.*, s.id as site_id, s.external_id as site_external_id " +
			"from user_addition ua " +
			"join user_addition_job uaj on ua.id = uaj.user_addition_id " +
			"join site s on ua.site_id = s.id " +
			"where ua.site_id = :site_id and ua.user_id = :user_id")
	Set<UserAdditionReadEntity> findAllBySiteIdAndUserId(@Param("site_id") UUID siteId, @Param("user_id") String userId);

	@Query(
		"select ua.* " +
		"from user_addition ua " +
		"join user_addition_job uaj on ua.id = uaj.user_addition_id " +
		"where uaj.correlation_id = :correlation_id"
	)
	Optional<UserAdditionSaveEntity> findByCorrelationId(@Param("correlation_id") UUID projectId);

	Set<UserAdditionSaveEntity> findAllByProjectId(UUID projectId);
	boolean existsBySiteIdAndUserId(UUID siteId, String userId);
	boolean existsByProjectIdAndUserId(UUID projectId, String userId);
}
