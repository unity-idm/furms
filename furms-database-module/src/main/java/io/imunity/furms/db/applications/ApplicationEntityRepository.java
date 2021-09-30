/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.db.applications;

import org.springframework.data.jdbc.repository.query.Modifying;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.Set;
import java.util.UUID;

interface ApplicationEntityRepository extends CrudRepository<ApplicationEntity, UUID> {
	Set<ApplicationEntity> findAllByProjectId(UUID projectId);
	Set<ApplicationEntity> findAllByUserId(String userId);
	@Modifying
	@Query("delete from application where project_id = :project_id and user_id = :user_id")
	void deleteByProjectIdAndUserId(@Param("project_id") UUID projectId, @Param("user_id") String userId);
	boolean existsByProjectIdAndUserId(UUID projectId, String userId);
}
