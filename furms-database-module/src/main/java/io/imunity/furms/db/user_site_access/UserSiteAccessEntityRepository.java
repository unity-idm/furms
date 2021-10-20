/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.db.user_site_access;

import org.springframework.data.jdbc.repository.query.Modifying;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.Set;
import java.util.UUID;

public interface UserSiteAccessEntityRepository extends CrudRepository<UserSiteAccessEntity, UUID> {
	Set<UserSiteAccessEntity> findAllBySiteIdAndUserId(UUID siteId, String userId);
	Set<UserSiteAccessEntity> findAllByProjectId(UUID projectId);

	boolean existsBySiteIdAndProjectIdAndUserId(UUID siteId, UUID projectId, String userId);

	@Modifying
	@Query("DELETE FROM user_site_access WHERE site_id = :siteId AND project_id = :projectId AND user_id = :userId")
	void deleteBy(@Param("siteId") UUID siteId, @Param("projectId") UUID projectId, @Param("userId") String userId);

	@Modifying
	@Query("DELETE FROM user_site_access WHERE project_id = :projectId AND user_id = :userId")
	void deleteBy(@Param("projectId") UUID projectId, @Param("userId") String userId);
}
