/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.db.sites;

import org.springframework.data.jdbc.repository.query.Modifying;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

interface SiteEntityRepository extends CrudRepository<SiteEntity, Long> {

	Optional<SiteEntity> findBySiteId(String siteId);

	boolean existsBySiteId(String siteId);

	boolean existsByName(String name);

	@Modifying
	void deleteBySiteId(String siteId);
}
