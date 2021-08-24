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
		"select ua.id as grant_id, ua.user_id as user_id, pa.project_id as project_id " +
			"from user_grant ua " +
			"join project_allocation pa on pa.id = ua.project_allocation_id " +
			"join user_grant_job uaj on ua.id = uaj.user_grant_id " +
			"where uaj.correlation_id = :correlation_id"
	)
	Optional<ProjectUserGrantEntity> findByCorrelationId(@Param("correlation_id") UUID correlationId);

	@Query(
		"select ua.*, s.external_id as site_external_id " +
			"from user_grant ua " +
			"join user_grant_job uaj on ua.id = uaj.user_grant_id " +
			"join site s on s.id = ua.site_id " +
			"where ua.user_id = :user_id and ua.project_id = :project_id and ua.site_id = :site_id and uaj.status = :status"
	)
	Set<UserGrantReadEntity> findByUserIdAndProjectIdAndSiteId(@Param("user_id") String userId, @Param("project_id") UUID projectId, @Param("site_id") UUID siteId, @Param("status") int status);

	Set<UserGrantEntity> findByUserIdAndProjectId(String userId, UUID projectId);

	@Query(
		"select ua.*, s.external_id as site_external_id " +
			"from user_grant ua " +
			"join user_grant_job uaj on ua.id = uaj.user_grant_id " +
			"join site s on s.id = ua.site_id " +
			"where ua.user_id = :user_id and ua.site_id = :site_id and uaj.status = :status"
	)
	Set<UserGrantReadEntity> findByUserIdAndSiteId(@Param("user_id") String userId, @Param("site_id") UUID siteId, @Param("status") int status);


	@Query(
		"select ua.id as allocation_id, ua.site_id as allocation_site_id, ua.project_Id as allocation_project_Id, " +
			"ua.project_allocation_id as allocation_project_allocation_id, ua.user_id as allocation_user_id, " +
			"uaj.id as job_id, uaj.correlation_id as job_correlation_id, uaj.user_grant_id as job_user_grant_id, uaj.status as job_status, uaj.message as job_message " +
			"from user_grant ua " +
			"join user_grant_job uaj on ua.id = uaj.user_grant_id " +
			"where ua.project_id = :project_id"
	)
	Set<UserGrantResolved> findAll(@Param("project_id") UUID projectId);

	@Query(
		"select ua.id as allocation_id, ua.site_id as allocation_site_id, ua.project_Id as allocation_project_Id, " +
			"ua.project_allocation_id as allocation_project_allocation_id, ua.user_id as allocation_user_id, " +
			"uaj.id as job_id, uaj.correlation_id as job_correlation_id, uaj.user_grant_id as job_user_grant_id, uaj.status as job_status, uaj.message as job_message " +
			"from user_grant ua " +
			"join user_grant_job uaj on ua.id = uaj.user_grant_id " +
			"where ua.project_id = :project_id and ua.user_id = :user_id"
	)
	Set<UserGrantResolved> findAll(@Param("project_id") UUID projectId, @Param("user_id") String userId);
}
