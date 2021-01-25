/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.db.sites;

import org.springframework.data.repository.CrudRepository;

import java.util.UUID;

interface SiteEntityRepository extends CrudRepository<SiteEntity, UUID> {

	boolean existsByName(String name);

	boolean existsByNameAndIdIsNot(String name, UUID id);
}
