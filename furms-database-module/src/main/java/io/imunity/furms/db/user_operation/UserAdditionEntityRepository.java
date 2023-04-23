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

	Set<UserAdditionSaveEntity> findAllByProjectId(UUID projectId);

	@Query(
			"select ua.*, uaj.*, s.id as site_id, s.external_id as site_external_id " +
			"from user_addition ua " +
			"join user_addition_job uaj on ua.id = uaj.user_addition_id " +
			"join site s on ua.site_id = s.id " +
			"where ua.project_id = :project_id and ua.user_id = :user_id"
	)
	Set<UserAdditionReadEntity> findAllByProjectIdAndUserId(@Param("project_id") UUID projectId, @Param("user_id") String userId);

	@Query(
		"select ua.*, uaj.*, s.id as site_id, s.external_id as site_external_id " +
			"from user_addition ua " +
			"join user_addition_job uaj on ua.id = uaj.user_addition_id " +
			"join site s on ua.site_id = s.id " +
			"where ua.project_id = :project_id and ua.site_id = :site_id"
	)
	Set<UserAdditionReadEntity> findAllBySiteIdAndProjectId(@Param("site_id") UUID siteId, @Param("project_id") UUID projectId);

	@Query(
		"select ua.*, uaj.*, s.id as site_id, s.external_id as site_external_id " +
			"from user_addition ua " +
			"join user_addition_job uaj on ua.id = uaj.user_addition_id " +
			"join site s on ua.site_id = s.id " +
			"where ua.site_id = :site_id and ua.project_id = :project_id and ua.user_id = :user_id"
	)
	Optional<UserAdditionReadEntity> findBySiteIdAndProjectIdAndUserId(@Param("site_id") UUID siteId, @Param("project_id") UUID projectId, @Param("user_id") String userId);

	@Query(
		"select ua.*, uaj.*, s.id as site_id, s.external_id as site_external_id " +
			"from user_addition ua " +
			"join user_addition_job uaj on ua.id = uaj.user_addition_id " +
			"join site s on ua.site_id = s.id " +
			"where ua.project_id = :project_id"
	)
	Set<UserAdditionReadEntity> findAllExtendedByProjectId(@Param("project_id") UUID projectId);

	@Query(
			"select ua.*, uaj.*, s.id as site_id, s.external_id as site_external_id " +
			"from user_addition ua " +
			"join user_addition_job uaj on ua.id = uaj.user_addition_id " +
			"join site s on ua.site_id = s.id " +
			"where ua.user_id = :user_id"
	)
	Set<UserAdditionReadEntity> findAllByUserId(@Param("user_id") String userId);

	@Query(
		"select ua.*, uaj.*, s.id as site_id, s.external_id as site_external_id " +
			"from user_addition ua " +
			"join user_addition_job uaj on ua.id = uaj.user_addition_id " +
			"join site s on ua.site_id = s.id " +
			"where ua.user_id = :user_id and s.id = :site_id"
	)
	Set<UserAdditionReadEntity> findAllBySiteIdAndUserId(@Param("site_id") UUID siteId,
	                                                     @Param("user_id") String userId);

	@Query("select ua.*, uaj.*, s.id as site_id, s.external_id as site_external_id " +
			"from user_addition ua " +
				"join user_addition_job uaj on ua.id = uaj.user_addition_id " +
				"join site s on ua.site_id = s.id " +
			"where s.id = :siteId")
	Set<UserAdditionReadEntity> findAllBySiteId(@Param("siteId") UUID siteId);


	@Query("select ua.*, uaj.*, s.id as site_id, s.external_id as site_external_id " +
		"from user_addition ua " +
		"join user_addition_job uaj on ua.id = uaj.user_addition_id " +
		"join site s on ua.site_id = s.id " +
		"where ua.project_id = :project_Id")
	Set<UserAdditionReadEntity> findExtendedAllByProjectId(@Param("project_Id") UUID projectId);

	@Query("SELECT ua.*," +
			"       uaj.*," +
			"       s.name AS site_name," +
			"       p.id AS project_id," +
			"       p.name AS project_name," +
			"       pij.gid AS gid" +
			" FROM user_addition ua" +
			"   JOIN user_addition_job uaj ON ua.id = uaj.user_addition_id" +
			"   JOIN site s ON ua.site_id = s.id" +
			"   JOIN project p ON ua.project_id = p.id" +
			"   LEFT JOIN project_installation_job pij ON ua.project_id = pij.project_Id AND ua.site_id = pij.site_Id" +
			" WHERE ua.site_id = :site_id AND ua.user_id = :user_id")
	Set<UserAdditionReadWithProjectsEntity> findAllWithSiteAndProjectsBySiteIdAndUserId(@Param("site_id") UUID siteId, @Param("user_id") String userId);

	@Query(
		"select ua.* " +
		"from user_addition ua " +
		"join user_addition_job uaj on ua.id = uaj.user_addition_id " +
		"where uaj.correlation_id = :correlation_id"
	)
	Optional<UserAdditionSaveEntity> findByCorrelationId(@Param("correlation_id") UUID correlationId);

	@Query(
		"select ua.*, uaj.*, s.id as site_id, s.external_id as site_external_id " +
			"from user_addition ua " +
			"join user_addition_job uaj on ua.id = uaj.user_addition_id " +
			"join site s on ua.site_id = s.id " +
			"where uaj.correlation_id = :correlation_id"
	)
	Optional<UserAdditionReadEntity> findReadEntityByCorrelationId(@Param("correlation_id") UUID projectId);

	@Query(
		"select uaj.status " +
			"from user_addition ua " +
			"join user_addition_job uaj on ua.id = uaj.user_addition_id " +
			"where ua.site_id = :site_id and ua.project_id = :project_id and ua.user_id = :user_id"
	)
	Optional<Integer> findStatusBySiteIdAndProjectIdAndUserId(@Param("site_id") UUID siteId, @Param("project_id") UUID projectId, @Param("user_id") String userId);

	@Query(
		"select uaj.status " +
			"from user_addition ua " +
			"join user_addition_job uaj on ua.id = uaj.user_addition_id " +
			"where ua.site_id = :site_id and ua.user_id = :user_id"
	)
	Set<Integer> findStatusBySiteIdAndUserId(@Param("site_id") UUID siteId, @Param("user_id") String userId);

	boolean existsBySiteIdAndUserId(UUID siteId, String userId);
	boolean existsBySiteIdAndProjectIdAndUserId(UUID siteId, UUID projectId, String userId);
}
