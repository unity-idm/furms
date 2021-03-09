/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.db.services;

import org.springframework.data.repository.CrudRepository;

import java.util.UUID;
import java.util.stream.Stream;

public interface ServiceEntityRepository extends CrudRepository<ServiceEntity, UUID> {
	Stream<ServiceEntity> findAllBySiteId(UUID siteId);
	boolean existsByName(String name);
}
