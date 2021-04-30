/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.db.user_operation;

import org.springframework.data.repository.CrudRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserRemovalEntityRepository extends CrudRepository<UserRemovalSaveEntity, UUID> {
	Optional<UserRemovalSaveEntity> findByCorrelationId(UUID correlationId);
}
