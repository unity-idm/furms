/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.db.user_site_access;

import io.imunity.furms.db.resource_access.UserGrantReadEntity;
import org.springframework.data.jdbc.repository.query.Modifying;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.Set;
import java.util.UUID;

public interface UserSiteAccessEntityRepository extends CrudRepository<UserSiteAccessEntity, UUID> {
	Set<UserSiteAccessEntity> findAllBySiteIdAndUserId(UUID siteId, String userId);

	boolean existsBySiteIdAndProjectIdAndUserId(UUID siteId, UUID projectId, String userId);

	@Modifying
	@Query("DELETE FROM user_site_access WHERE site_id = :siteId AND project_id = :projectId AND user_id = :userId")
	void deleteBy(@Param("siteId") UUID siteId, @Param("projectId") UUID projectId, @Param("userId") String userId);

	@Query(
		"SELECT usa.*, ugj.status, uaj.status " +
			"FROM user_site_access usa " +
			"LEFT JOIN user_grant ug ON ug.site_id = usa.site_id AND  ug.project_id = usa.project_id AND  ug.user_id = usa.user_id " +
			"JOIN user_grant_job ugj ON ug.id = ugj.user_grant_id " +
			"LEFT JOIN user_addition ua ON ua.site_id = usa.site_id AND  ua.project_id = usa.project_id AND  ua.user_id = usa.user_id " +
			"JOIN user_addition_job uaj ON uaj.site_id = usa.site_id AND  uaj.project_id = usa.project_id AND  uaj.user_id = usa.user_id " +
			"WHERE ua.user_id = :user_id AND ua.project_id = :project_id AND ua.site_id = :site_id"
	)
	Set<UserGrantReadEntity> findByUserIdAndProjectIdAndSiteId(@Param("user_id") String userId, @Param("project_id") UUID projectId, @Param("site_id") UUID siteId);
}
