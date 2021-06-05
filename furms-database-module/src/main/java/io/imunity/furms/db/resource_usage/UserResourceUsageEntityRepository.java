/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.db.resource_usage;

import org.springframework.data.repository.CrudRepository;

import java.util.UUID;

public interface UserResourceUsageEntityRepository extends CrudRepository<UserResourceUsageEntity, UUID> {
}
