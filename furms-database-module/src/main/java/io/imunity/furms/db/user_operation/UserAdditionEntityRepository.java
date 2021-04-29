/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.db.user_operation;

import io.imunity.furms.domain.user_operation.UserAdditionStatus;
import io.imunity.furms.domain.user_operation.UserRemovalStatus;
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
			"join site s on ua.site_id = s.id " +
			"where ua.project_id = :project_id and ua.user_id = :user_id"
	)
	Set<UserAdditionReadEntity> findAllByProjectIdAndUserId(@Param("project_id") UUID projectId, @Param("user_id") String userId);

	@Query(
			"select count(ua.id) > 0 " +
			"from user_addition ua " +
			"left join user_removal ur on ua.id = ur.user_addition_id " +
			"where ua.user_id = :user_id and ua.status = :addition_status and (ur.id is null or ur.status != :removal_status)"
	)
	boolean isUserAdded(@Param("user_id") String userId, @Param("addition_status") UserAdditionStatus additionStatus, @Param("removal_status") UserRemovalStatus removalStatus);
}
