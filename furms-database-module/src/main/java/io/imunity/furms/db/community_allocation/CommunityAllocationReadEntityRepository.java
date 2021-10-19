/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.db.community_allocation;

import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public interface CommunityAllocationReadEntityRepository extends CrudRepository<CommunityAllocationReadEntity, UUID> {
	@Override
	@Query("select a.*, " +
		"s.id as site_id, s.name as site_name, s.connection_info as site_connection_info , s.logo as site_logo , s.logo_type as site_logo_type, " +
		"rt.id as resourceType_id, rt.name as resourceType_name, rt.site_id as resourceType_site_id, rt.service_id as resourceType_service_id, rt.type as resourceType_type, rt.unit as resourceType_unit, rt.accessible as resourceType_accessible, " +
		"rc.id as resourceCredit_id, rc.name as resourceCredit_name, rc.site_id as resourceCredit_site_id, rc.resource_type_id as resourceCredit_resource_type_id, rc.split as resourceCredit_split, rc.amount as resourceCredit_amount, rc.create_time as resourceCredit_create_time, rc.start_time as resourceCredit_start_time, rc.end_time as resourceCredit_end_time, " +
		"c.name as community_name " +
		"from community_allocation a " +
		"join resource_credit rc on a.resource_credit_id = rc.id " +
		"join site s on rc.site_id = s.id " +
		"join community c on a.community_id = c.id " +
		"join resource_type rt on rc.resource_type_id = rt.id " +
		"where a.id = :id")
	Optional<CommunityAllocationReadEntity> findById(@Param("id") UUID id);

	@Query("select a.*, " +
		"s.id as site_id, s.name as site_name, s.connection_info as site_connection_info , s.logo as site_logo , s.logo_type as site_logo_type, " +
		"rt.id as resourceType_id, rt.name as resourceType_name, rt.site_id as resourceType_site_id, rt.service_id as resourceType_service_id, rt.type as resourceType_type, rt.unit as resourceType_unit, rt.accessible as resourceType_accessible, " +
		"rc.id as resourceCredit_id, rc.name as resourceCredit_name, rc.site_id as resourceCredit_site_id, rc.resource_type_id as resourceCredit_resource_type_id, rc.split as resourceCredit_split, rc.amount as resourceCredit_amount, rc.create_time as resourceCredit_create_time, rc.start_time as resourceCredit_start_time, rc.end_time as resourceCredit_end_time, " +
		"c.name as community_name " +
		"from community_allocation a " +
		"join resource_credit rc on a.resource_credit_id = rc.id " +
		"join site s on rc.site_id = s.id " +
		"join community c on a.community_id = c.id " +
		"join resource_type rt on rc.resource_type_id = rt.id " +
		"where a.community_id = :id")
	Set<CommunityAllocationReadEntity> findAllByCommunityId(@Param("id") UUID id);

	@Query("SELECT a.*, " +
			"   s.id AS site_id, s.name AS site_name, s.connection_info AS site_connection_info, s.logo AS site_logo, " +
			"   s.logo_type AS site_logo_type, rt.id AS resourceType_id, rt.name AS resourceType_name, " +
			"   rt.site_id AS resourceType_site_id, rt.service_id AS resourceType_service_id, rt.type AS resourceType_type, " +
			"   rt.unit AS resourceType_unit, rc.id AS resourceCredit_id, rc.name AS resourceCredit_name, rt.accessible as resourceType_accessible, " +
			"   rc.site_id AS resourceCredit_site_id, rc.resource_type_id AS resourceCredit_resource_type_id, " +
			"   rc.split AS resourceCredit_split, rc.amount AS resourceCredit_amount, " +
			"   rc.create_time AS resourceCredit_create_time, rc.start_time AS resourceCredit_start_time, " +
			"   rc.end_time AS resourceCredit_end_time, " +
			"   c.name as community_name " +
			"FROM community_allocation a " +
			"   JOIN resource_credit rc ON a.resource_credit_id = rc.id " +
			"   JOIN site s ON rc.site_id = s.id " +
			"   JOIN community c ON a.community_id = c.id " +
			"   JOIN resource_type rt ON rc.resource_type_id = rt.id " +
			"WHERE a.community_id = :id " +
			"   AND (UPPER(a.name) LIKE UPPER(CONCAT('%', :name, '%')) " +
			"        OR UPPER(s.name) LIKE UPPER(CONCAT('%', :name, '%')))")
	Set<CommunityAllocationReadEntity> findAllByCommunityIdAndNameOrSiteName(@Param("id") UUID id,
	                                                                         @Param("name") String name);

	@Query("select rc.amount as resource_credit_amount, sum(ca.amount) as community_allocations_amount " +
		"from resource_credit rc " +
		"left join community_allocation ca on ca.resource_credit_id = rc.id " +
		"where rc.id = :id " +
		"group by rc.amount")
	CommunityAllocationSum calculateAvailableAmount(@Param("id") UUID resourceCreditId);
}
