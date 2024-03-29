/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.db.services;

import org.springframework.data.repository.CrudRepository;

import java.util.Set;
import java.util.UUID;

public interface InfraServiceEntityRepository extends CrudRepository<InfraServiceEntity, UUID> {
	Set<InfraServiceEntity> findAllBySiteId(UUID siteId);
	boolean existsByNameAndSiteId(String name, UUID siteId);
}
