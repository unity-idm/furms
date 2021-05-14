/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.db.resource_access;

import org.springframework.data.repository.CrudRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserGrantJobEntityRepository extends CrudRepository<UserGrantJobEntity, UUID> {
	Optional<UserGrantJobEntity> findByCorrelationId(UUID correlationId);
	Optional<UserGrantJobEntity> findByUserAllocationId(UUID userAllocationId);
}
