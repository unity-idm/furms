/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.db.resource_access;

import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public interface UserGrantEntityRepository extends CrudRepository<UserGrantEntity, UUID> {
	@Query(
		"select ua.id as allocation_id, ua.site_id as allocation_site_id, ua.project_Id as allocation_project_Id, " +
		"ua.project_allocation_id as allocation_project_allocation_id, ua.user_id as allocation_user_id, " +
		"uaj.id as job_id, uaj.correlation_id as job_correlation_id, uaj.user_grant_id as job_user_grant_id, uaj.status as job_status, uaj.message as job_message " +
		"from user_grant ua " +
		"join user_grant_job uaj on ua.id = uaj.user_grant_id " +
		"where ua.user_id = :user_id and ua.project_allocation_id = :project_allocation_id"
	)
	Optional<UserGrantResolved> findByUserIdAndProjectAllocationId(@Param("user_id") String userId, @Param("project_allocation_id") UUID projectAllocationId);

	@Query(
		"select ua.id as allocation_id, ua.site_id as allocation_site_id, ua.project_Id as allocation_project_Id, " +
			"ua.project_allocation_id as allocation_project_allocation_id, ua.user_id as allocation_user_id, " +
			"uaj.id as job_id, uaj.correlation_id as job_correlation_id, uaj.user_grant_id as job_user_grant_id, uaj.status as job_status, uaj.message as job_message " +
			"from user_grant ua " +
			"join user_grant_job uaj on ua.id = uaj.user_grant_id " +
			"where ua.project_id = :project_id"
	)
	Set<UserGrantResolved> findAll(@Param("project_id") UUID projectId);
}
