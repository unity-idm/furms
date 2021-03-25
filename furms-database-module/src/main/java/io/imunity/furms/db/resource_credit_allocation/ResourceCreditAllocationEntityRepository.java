/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.db.resource_credit_allocation;

import org.springframework.data.repository.CrudRepository;

import java.util.UUID;

public interface ResourceCreditAllocationEntityRepository extends CrudRepository<ResourceCreditAllocationEntity, UUID> {
	boolean existsByName(String name);
	boolean existsByResourceCreditId(UUID resourceCreditId);
}
