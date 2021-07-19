/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.db.project_installation;

import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public interface ProjectInstallationJobEntityRepository extends CrudRepository<ProjectInstallationJobEntity, UUID> {
	Optional<ProjectInstallationJobEntity> findByCorrelationId(UUID correlationId);
	Set<ProjectInstallationJobEntity> findByProjectId(UUID projectId);

	boolean existsBySiteIdAndProjectIdAndStatus(UUID siteId, UUID projectId, int status);
	boolean existsByProjectIdAndStatusOrProjectIdAndStatus(UUID projectId, int pendingStatus, UUID projectId1, int ackStatus);

	@Query("select s.id as site_id, " +
					"s.name as site_name, " +
					"pij.project_id as project_id, " +
					"pij.status as status, " +
					"pij.message as message, " +
					"pij.code as code, " +
					"pij.gid as gid " +
					"from project_installation_job pij " +
					"join site s on pij.site_id = s.id " +
					"join project p on pij.project_id = p.id " +
					"where s.id = :siteId")
	Set<ProjectInstallationJobStatusEntity> findAllBySiteId(@Param("siteId") UUID siteId);

	@Query("SELECT s.id AS site_id, " +
					"s.name AS site_name, " +
					"pij.project_id AS project_id, " +
					"pij.status AS status, " +
					"pij.message AS message, " +
					"pij.code AS code, " +
					"pij.gid AS gid " +
					"FROM project_installation_job pij " +
					"JOIN site s ON pij.site_id = s.id " +
					"JOIN project p ON pij.project_id = p.id " +
					"WHERE pij.status = 2 " +
					" AND s.id = :siteId")
	Set<ProjectInstallationJobStatusEntity> findAllInstalledBySiteId(@Param("siteId") UUID siteId);

	@Query(
		"select s.id as site_id, s.name as site_name, pij.project_id as project_id, pij.status as status, pij.message as message, pij.code as code " +
			"from project_installation_job pij " +
			"join site s on pij.site_id = s.id " +
			"join project p on pij.project_id = p.id " +
			"join community c on p.community_id = c.id " +
			"where c.id = :id")
	Set<ProjectInstallationJobStatusEntity> findAllByCommunityId(@Param("id") UUID communityId);

	@Query(
		"select s.id as site_id, s.name as site_name, pij.project_id as project_id, pij.status as status, pij.message as message, pij.code as code " +
			"from project_installation_job pij " +
			"join site s on pij.site_id = s.id " +
			"where pij.project_id = :id")
	Set<ProjectInstallationJobStatusEntity> findAllByProjectId(@Param("id") UUID projectId);

	@Query(
		"select p.id as id, s.id as site_id, s.external_id as site_external_id, p.name as name, p.description as description, c.id as community_id, " +
			"c.name as community_name, p.acronym as acronym, p.research_field as research_field, p.start_time as validity_start, p.end_time as validity_end, p.leader_id as leader_id " +
			"from project_allocation pa " +
			"join community_allocation ca on pa.community_allocation_id = ca.id " +
			"join resource_credit rc on ca.resource_credit_id = rc.id " +
			"join site s on rc.site_id = s.id " +
			"join community c on ca.community_id = c.id " +
			"join project p on pa.project_id = p.id " +
			"where pa.id = :id")
	ProjectInstallationEntity findByProjectAllocationId(@Param("id") UUID id);
}
