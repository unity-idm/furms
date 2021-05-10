/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.db.services;

import org.springframework.data.repository.CrudRepository;

import java.util.UUID;
import java.util.stream.Stream;

public interface InfraServiceEntityRepository extends CrudRepository<InfraServiceEntity, UUID> {
	Stream<InfraServiceEntity> findAllBySiteId(UUID siteId);
	boolean existsByNameAndSiteId(String name, UUID siteId);
}
