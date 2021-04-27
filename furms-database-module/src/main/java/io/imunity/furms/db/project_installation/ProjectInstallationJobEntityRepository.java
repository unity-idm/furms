/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.db.project_installation;

import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.UUID;

public interface ProjectInstallationJobEntityRepository extends CrudRepository<ProjectInstallationJobEntity, UUID> {
	ProjectInstallationJobEntity findByCorrelationId(UUID correlationId);

	boolean existsByProjectId(UUID projectId);

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
