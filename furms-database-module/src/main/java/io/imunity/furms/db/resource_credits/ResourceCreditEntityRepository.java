/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.db.resource_credits;

import org.springframework.data.repository.CrudRepository;

import java.util.Collection;
import java.util.UUID;
import java.util.stream.Stream;

public interface ResourceCreditEntityRepository extends CrudRepository<ResourceCreditEntity, UUID> {
	Stream<ResourceCreditEntity> findAllBySiteId(UUID siteId);
	boolean existsByName(String name);
	boolean existsBySiteId(UUID siteId);
	boolean existsByResourceTypeId(UUID resourceTypeId);
	boolean existsByResourceTypeIdIn(Collection<UUID> resourceTypeIds);
}
