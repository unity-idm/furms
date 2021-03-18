/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.db.resource_types;

import org.springframework.data.repository.CrudRepository;

import java.util.UUID;
import java.util.stream.Stream;

public interface ResourceTypeEntityRepository extends CrudRepository<ResourceTypeEntity, UUID> {
	Stream<ResourceTypeEntity> findAllBySiteId(UUID siteId);
	Stream<ResourceTypeEntity> findAllByServiceId(UUID serviceId);
	boolean existsByName(String name);
}
