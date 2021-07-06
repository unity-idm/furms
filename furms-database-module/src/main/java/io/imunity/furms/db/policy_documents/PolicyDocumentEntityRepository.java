/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.db.policy_documents;

import org.springframework.data.repository.CrudRepository;

import java.util.Set;
import java.util.UUID;

public interface PolicyDocumentEntityRepository extends CrudRepository<PolicyDocumentEntity, UUID> {
	Set<PolicyDocumentEntity> findAllBySiteId(UUID siteId);
	boolean existsBySiteIdAndName(UUID siteId, String name);
}
