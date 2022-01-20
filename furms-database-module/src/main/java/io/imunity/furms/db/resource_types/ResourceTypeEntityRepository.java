/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.db.resource_types;

import org.springframework.data.repository.CrudRepository;

import java.util.Set;
import java.util.UUID;

public interface ResourceTypeEntityRepository extends CrudRepository<ResourceTypeEntity, UUID> {
	Set<ResourceTypeEntity> findAllBySiteId(UUID siteId);
	Set<ResourceTypeEntity> findAllByServiceId(UUID serviceId);
	boolean existsByNameAndSiteId(String name, UUID siteId);
}
