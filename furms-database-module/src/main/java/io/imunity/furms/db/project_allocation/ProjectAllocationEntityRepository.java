/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.db.project_allocation;

import org.springframework.data.repository.CrudRepository;

import java.util.UUID;

public interface ProjectAllocationEntityRepository extends CrudRepository<ProjectAllocationEntity, UUID> {
	boolean existsByName(String name);
	boolean existsByResourceCreditId(UUID resourceCreditId);
}
