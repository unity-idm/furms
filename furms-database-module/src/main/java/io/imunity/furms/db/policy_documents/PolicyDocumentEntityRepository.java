/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.db.policy_documents;

import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public interface PolicyDocumentEntityRepository extends CrudRepository<PolicyDocumentEntity, UUID> {
	Set<PolicyDocumentEntity> findAllBySiteId(UUID siteId);
	boolean existsBySiteIdAndName(UUID siteId, String name);

	@Query(
		"select pd.* " +
			"from policy_document pd " +
			"join service s on s.policy_id = pd.id " +
			"join resource_type rt on s.id = rt.service_id " +
			"join resource_credit rc on rt.id = rc.resource_type_id " +
			"join community_allocation ca on rc.id = ca.resource_credit_id " +
			"join project_allocation pa on ca.id = pa.community_allocation_id " +
			"join user_grant ug on pa.id = ug.project_allocation_id " +
			"where ug.id = :user_grant_id"
	)
	Optional<PolicyDocumentEntity> findByUserGrantId(@Param("user_grant_id") UUID userGrantId);

	@Query("select pd.*, s.name as site_name, se.name as service_name " +
		"from policy_document pd " +
		"join service se on pd.id = se.policy_id " +
		"join site s on se.site_id = s.id " +
		"join resource_type rt on se.id = rt.service_id " +
		"join resource_credit rc on rc.resource_type_id = rt.id " +
		"join community_allocation ca on rc.id = ca.resource_credit_id " +
		"join project_allocation pa on ca.id = pa.community_allocation_id " +
		"join user_grant ua on ua.project_allocation_id = pa.id " +
		"where ua.user_id = :user_id")
	Set<PolicyDocumentExtendedEntity> findAllServicePoliciesByUserId(@Param("user_id") String userId);

	@Query("select pd.id as policy_id, ua.user_id as user_id, pd.revision as revision " +
		"from policy_document pd " +
		"join service se on pd.id = se.policy_id " +
		"join site s on se.site_id = s.id " +
		"join resource_type rt on se.id = rt.service_id " +
		"join resource_credit rc on rc.resource_type_id = rt.id " +
		"join community_allocation ca on rc.id = ca.resource_credit_id " +
		"join project_allocation pa on ca.id = pa.community_allocation_id " +
		"join user_grant ua on ua.project_allocation_id = pa.id " +
		"where s.id = :site_id")
	Set<UserWithBasePolicy> findAllUsersWithServicesPolicies(@Param("site_id") String siteId);

	@Query("select pd.id as policy_id, ua.user_id as user_id, pd.revision as revision " +
		"from site s " +
		"join policy_document pd on s.policy_id = pd.id " +
		"join user_grant ua on pd.site_id = ua.site_id " +
		"where s.id = :site_id")
	Set<UserWithBasePolicy> findAllUsersWithSitePolicy(@Param("site_id") String siteId);

	@Query("select pd.*, s.name as site_name " +
		"from policy_document pd " +
		"join site s on pd.id = s.policy_id " +
		"join user_grant ua on ua.site_id = s.id " +
		"where ua.user_id = :user_id")
	Set<PolicyDocumentExtendedEntity> findAllSitePoliciesByUserId(@Param("user_id") String userId);

	@Query("select pd.*, s.id as service_id " +
		"from service s " +
		"join policy_document pd on pd.id = s.policy_id " +
		"where s.site_id = :site_id")
	Set<ServicePolicyDocumentEntity> findAllServicePoliciesBySiteId(@Param("site_id") UUID siteId);
}
