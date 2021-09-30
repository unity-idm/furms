/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.db.generic_groups;

import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public interface GenericGroupAssignmentEntityRepository extends CrudRepository<GenericGroupAssignmentEntity, UUID> {
	Set<GenericGroupAssignmentEntity> findAllByGenericGroupId(UUID genericGroupId);

	@Query("select gga.* " +
		"from generic_group gg " +
		"join generic_group_assignment gga on gg.id = gga.generic_group_id " +
		"where gg.community_id = :community_id and gga.id = :id")
	Optional<GenericGroupAssignmentEntity> findByCommunityIdAndId(@Param("community_id") UUID communityId, @Param("id") UUID id);

	boolean existsByGenericGroupIdAndUserId(UUID genericGroupId, String userId);
}