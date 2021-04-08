/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.db.sites;

import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

interface SiteEntityRepository extends CrudRepository<SiteEntity, UUID> {

	boolean existsByName(String name);

	boolean existsByShortId(String shortId);

	boolean existsByNameAndIdIsNot(String name, UUID id);

	@Query("select short_id from site s where s.id = :id")
	Optional<String> findShortId(@Param("id") UUID id);
}
