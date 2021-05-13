/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.db.resource_access;

import org.springframework.data.repository.CrudRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserAllocationJobRepository extends CrudRepository<UserAllocationJobEntity, UUID> {
	Optional<UserAllocationJobEntity> findByCorrelationId(UUID correlationId);
	Optional<UserAllocationJobEntity> findByUserAllocationId(UUID userAllocationId);
}
