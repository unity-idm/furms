/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.db.generic_groups;

import org.springframework.data.jdbc.repository.query.Modifying;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public interface GenericGroupMembershipEntityRepository extends CrudRepository<GenericGroupMembershipEntity, UUID> {
	Set<GenericGroupMembershipEntity> findAllByGenericGroupId(UUID genericGroupId);

	@Query("select gga.* " +
		"from generic_group gg " +
		"join generic_group_membership gga on gg.id = gga.generic_group_id " +
		"where gg.community_id = :community_id and gga.id = :id")
	Optional<GenericGroupMembershipEntity> findByCommunityIdAndId(@Param("community_id") UUID communityId, @Param("id") UUID id);

	boolean existsByGenericGroupIdAndUserId(UUID genericGroupId, String userId);

	@Modifying
	@Query("delete from generic_group_membership where generic_group_id = :generic_group_id and user_id = :user_id")
	void deleteByGenericGroupIdAndUserId(@Param("generic_group_id") UUID genericGroupId, @Param("user_id") String userId);
}