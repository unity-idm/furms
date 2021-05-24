/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.db.project_allocation;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

public interface ProjectAllocationReadEntityRepository extends CrudRepository<ProjectAllocationReadEntity, UUID> {
	@Override
	@Query("select a.*, " +
		"s.id as site_id, s.external_id as site_external_id, s.name as site_name, s.connection_info as site_connection_info , s.logo as site_logo , s.logo_type as site_logo_type, " +
		"rt.id as resourceType_id, rt.name as resourceType_name, rt.site_id as resourceType_site_id, rt.service_id as resourceType_service_id, rt.type as resourceType_type, rt.unit as resourceType_unit, " +
		"rc.id as resourceCredit_id, rc.name as resourceCredit_name, rc.site_id as resourceCredit_site_id, rc.resource_type_id as resourceCredit_resource_type_id, rc.split as resourceCredit_split, rc.access as resourceCredit_access, rc.amount as resourceCredit_amount, rc.create_time as resourceCredit_create_time, rc.start_time as resourceCredit_start_time, rc.end_time as resourceCredit_end_time, " +
		"ca.id as communityAllocation_id, ca.name as communityAllocation_name, ca.resource_credit_id as communityAllocation_resource_credit_id, ca.amount as communityAllocation_amount, ca.community_id as communityAllocation_community_id " +
		"from project_allocation a " +
		"join community_allocation ca on a.community_allocation_id = ca.id " +
		"join resource_credit rc on ca.resource_credit_id = rc.id " +
		"join site s on rc.site_id = s.id " +
		"join resource_type rt on rc.resource_type_id = rt.id " +
		"where a.id = :id")
	Optional<ProjectAllocationReadEntity> findById(@Param("id") UUID id);

	@Query("select a.*, " +
		"s.id as site_id, s.name as site_name, s.connection_info as site_connection_info, s.logo as site_logo, s.logo_type as site_logo_type, s.external_id as site_external_id, " +
		"rt.id as resourceType_id, rt.name as resourceType_name, rt.site_id as resourceType_site_id, rt.service_id as resourceType_service_id, rt.type as resourceType_type, rt.unit as resourceType_unit, " +
		"rc.id as resourceCredit_id, rc.name as resourceCredit_name, rc.site_id as resourceCredit_site_id, rc.resource_type_id as resourceCredit_resource_type_id, rc.split as resourceCredit_split, rc.access as resourceCredit_access, rc.amount as resourceCredit_amount, rc.create_time as resourceCredit_create_time, rc.start_time as resourceCredit_start_time, rc.end_time as resourceCredit_end_time, " +
		"ca.id as communityAllocation_id, ca.name as communityAllocation_name, ca.resource_credit_id as communityAllocation_resource_credit_id, ca.amount as communityAllocation_amount, ca.community_id as communityAllocation_community_id " +
		"from project_allocation a " +
		"join community_allocation ca on a.community_allocation_id = ca.id " +
		"join resource_credit rc on ca.resource_credit_id = rc.id " +
		"join site s on rc.site_id = s.id " +
		"join resource_type rt on rc.resource_type_id = rt.id " +
		"where a.project_id = :id")
	Set<ProjectAllocationReadEntity> findAllByProjectId(@Param("id") UUID id);

	@Query("select ca.amount as community_allocation_amount, sum(pa.amount) as project_allocations_amount " +
		"from community_allocation ca " +
		"left join project_allocation pa on pa.community_allocation_id = ca.id " +
		"where ca.id = :id " +
		"group by ca.amount")
	ProjectAllocationSum calculateAvailableAmount(@Param("id") UUID communityAllocationId);

	@Query(
		"select count(*) > 0 " +
		"from project_allocation pa " +
		"join community_allocation ca on pa.community_allocation_id = ca.id " +
		"where ca.community_id = :communityId and pa.name = :name")
	boolean existsByCommunityIdAndName(@Param("communityId") UUID communityId, @Param("name") String name);

}
