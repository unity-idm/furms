/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.db.sites;

import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

interface SiteEntityRepository extends CrudRepository<SiteEntity, UUID> {

	boolean existsByName(String name);

	boolean existsByExternalId(String shortId);

	boolean existsByNameAndIdIsNot(String name, UUID id);

	@Query("select external_id from site s where s.id = :id")
	Optional<String> findExternalId(@Param("id") UUID id);

	@Query(
			"select s.* " +
			"from site s " +
			"join resource_credit rs on rs.site_id = s.id " +
			"join community_allocation ca on ca.resource_credit_id = rs.id " +
			"join project_allocation pa on pa.community_allocation_id = ca.id " +
			"where pa.project_id = :id"
	)
	Set<SiteEntity> findRelatedSites(@Param("id") UUID projectId);

	@Query(
		"select ca.community_id as community_id, pa.project_id as project_id " +
			"from site s " +
			"join resource_credit rs on rs.site_id = s.id " +
			"join community_allocation ca on ca.resource_credit_id = rs.id " +
			"join project_allocation pa on pa.community_allocation_id = ca.id " +
			"where s.id = :id"
	)
	Set<CommunityAndProjectIdHolder> findRelatedProjectIds(@Param("id") UUID siteId);
}
