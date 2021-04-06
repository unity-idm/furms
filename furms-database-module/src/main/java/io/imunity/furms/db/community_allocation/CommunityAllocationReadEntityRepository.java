/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.db.community_allocation;

import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public interface CommunityAllocationReadEntityRepository extends CrudRepository<CommunityAllocationReadEntity, UUID> {
	@Query("select a.*, " +
		"s.id as site_id, s.name as site_name, s.connection_info as site_connection_info , s.logo as site_logo , s.logo_type as site_logo_type, " +
		"rt.id as resourceType_id, rt.name as resourceType_name, rt.site_id as resourceType_site_id, rt.service_id as resourceType_service_id, rt.type as resourceType_type, rt.unit as resourceType_unit, " +
		"rc.id as resourceCredit_id, rc.name as resourceCredit_name, rc.site_id as resourceCredit_site_id, rc.resource_type_id as resourceCredit_resource_type_id, rc.split as resourceCredit_split, rc.access as resourceCredit_access, rc.amount as resourceCredit_amount, rc.create_time as resourceCredit_create_time, rc.start_time as resourceCredit_start_time, rc.end_time as resourceCredit_end_time " +
		"from community_allocation a " +
		"join resource_credit rc on a.resource_credit_id = rc.id " +
		"join site s on rc.site_id = s.id " +
		"join resource_type rt on rc.resource_type_id = rt.id " +
		"where a.id = :id")
	Optional<CommunityAllocationReadEntity> findById(@Param("id") UUID id);

	@Query("select a.*, " +
		"s.id as site_id, s.name as site_name, s.connection_info as site_connection_info , s.logo as site_logo , s.logo_type as site_logo_type, " +
		"rt.id as resourceType_id, rt.name as resourceType_name, rt.site_id as resourceType_site_id, rt.service_id as resourceType_service_id, rt.type as resourceType_type, rt.unit as resourceType_unit, " +
		"rc.id as resourceCredit_id, rc.name as resourceCredit_name, rc.site_id as resourceCredit_site_id, rc.resource_type_id as resourceCredit_resource_type_id, rc.split as resourceCredit_split, rc.access as resourceCredit_access, rc.amount as resourceCredit_amount, rc.create_time as resourceCredit_create_time, rc.start_time as resourceCredit_start_time, rc.end_time as resourceCredit_end_time " +
		"from community_allocation a " +
		"join resource_credit rc on a.resource_credit_id = rc.id " +
		"join site s on rc.site_id = s.id " +
		"join resource_type rt on rc.resource_type_id = rt.id " +
		"where a.community_id = :id")
	Set<CommunityAllocationReadEntity> findAllByCommunityId(@Param("id") UUID id);


	@Query("select rc.amount - isnull(sum(ca.amount),0) " +
		"from resource_credit rc " +
		"left join community_allocation ca on ca.resource_credit_id = rc.id " +
		"where rc.id = :id")
	BigDecimal calculateAvailableAmount(@Param("id") UUID resourceCreditId);
}
