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
	Optional<UserAdditionSaveEntity> findByCorrelationId(UUID correlationId);

	@Query(
			"select ua.*, s.id as site_id, s.external_id as site_external_id " +
			"from user_addition ua " +
			"left join user_removal ur on ua.id = ur.user_addition_id " +
			"join site s on ua.site_id = s.id " +
			"where ua.project_id = :project_id and ua.user_id = :user_id and ur.id is null"
	)
	Set<UserAdditionReadEntity> findAllByProjectIdAndUserId(@Param("project_id") UUID projectId, @Param("user_id") String userId);

	@Query(
			"select count(ua.id) > 0 " +
			"from user_addition ua " +
			"left join user_removal ur on ua.id = ur.user_addition_id " +
			"where ua.site_id = :site_id and ua.user_id = :user_id and ua.status = :addition_status and (ur.id is null or ur.status != :removal_status)"
	)
	boolean isUserAdded(@Param("site_id") UUID siteId, @Param("user_id") String userId, @Param("addition_status") int additionStatus, @Param("removal_status") int removalStatus);

	@Query(
			"select ua.user_id " +
			"from user_addition ua " +
			"left join user_removal ur on ua.id = ur.user_addition_id " +
			"where ua.project_id = :project_id and ua.status = :addition_status and (ur.id is null or ur.status != :removal_status)"
	)
	Set<String> findAddedUserIds(@Param("project_id") UUID projectId, @Param("addition_status") int additionStatus, @Param("removal_status") int removalStatus);
}
