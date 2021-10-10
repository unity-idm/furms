/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.db.generic_groups;

import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.Set;
import java.util.UUID;

public interface GenericGroupEntityRepository extends CrudRepository<GenericGroupEntity, UUID> {
	@Query("select gg.*, gga.user_id as user_id, gga.member_since as member_since " +
		"from generic_group gg " +
		"left join generic_group_membership gga on gg.id = gga.generic_group_id " +
		"where gg.community_id = :community_id and gg.id = :group_id")
	Set<GenericGroupEntityWithMembership> findAllAssignments(@Param("community_id") UUID communityId, @Param("group_id") UUID groupId);

	@Query("select gg.*, gga.user_id as user_id, gga.member_since as member_since " +
		"from generic_group gg " +
		"join generic_group_membership gga on gg.id = gga.generic_group_id " +
		"where gga.user_id = :user_id")
	Set<GenericGroupEntityWithMembership> findAllAssignments(@Param("user_id") String userId);

	@Query("select gg.*, count(gga.id) as membership_amount " +
		"from generic_group gg " +
		"left join generic_group_membership gga on gg.id = gga.generic_group_id " +
		"where gg.community_id = :community_id " +
		"group by gg.id")
	Set<GenericGroupEntityWithMembershipAmount> findAllWithAssignmentAmount(@Param("community_id") UUID communityId);

	Set<GenericGroupEntity> findAllByCommunityId(UUID communityId);

	boolean existsByCommunityIdAndId(UUID communityId, UUID id);

	boolean existsByCommunityIdAndName(UUID communityId, String name);
}