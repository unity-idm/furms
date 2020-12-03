/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.db.sites;

import org.springframework.data.jdbc.repository.query.Modifying;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

interface SiteEntityRepository extends CrudRepository<SiteEntity, Long> {

	@Query("SELECT * FROM site WHERE site_id = :siteId")
	Optional<SiteEntity> findBySiteId(@Param("siteId") String siteId);

	@Query("SELECT COUNT(id) = 1 FROM site WHERE site_id = :siteId")
	boolean existsBySiteId(@Param("siteId") String siteId);

	@Query("SELECT COUNT(id) = 1 FROM site WHERE name = :name")
	boolean existsByName(@Param("name") String name);

	@Modifying
	@Query("DELETE FROM site WHERE site_id = :siteId")
	void deleteBySiteId(String siteId);
}
